package com.taller360.clients.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller360.audit.AuditPort;
import com.taller360.clients.dto.ClientDtos.*;
import com.taller360.clients.entity.Client;
import com.taller360.clients.entity.ClientCommunication;
import com.taller360.clients.entity.ClientContact;
import com.taller360.clients.entity.ClientDocument;
import com.taller360.clients.port.Client360ExternalPort;
import com.taller360.clients.repository.ClientCommunicationRepository;
import com.taller360.clients.repository.ClientContactRepository;
import com.taller360.clients.repository.ClientDocumentRepository;
import com.taller360.clients.repository.ClientRepository;
import com.taller360.common.tenant.TenantGuard;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import com.taller360.organization.repository.BranchRepository;
import com.taller360.organization.repository.CompanyRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {
  private final ClientRepository clients;
  private final ClientContactRepository contacts;
  private final ClientDocumentRepository documents;
  private final ClientCommunicationRepository communications;
  private final CompanyRepository companies;
  private final BranchRepository branches;
  private final TenantGuard tenant;
  private final AuditPort audit;
  private final Client360ExternalPort external360;
  private final ObjectMapper objectMapper;

  public ClientService(ClientRepository clients, ClientContactRepository contacts, ClientDocumentRepository documents,
      ClientCommunicationRepository communications, CompanyRepository companies, BranchRepository branches,
      TenantGuard tenant, AuditPort audit, Client360ExternalPort external360, ObjectMapper objectMapper) {
    this.clients = clients;
    this.contacts = contacts;
    this.documents = documents;
    this.communications = communications;
    this.companies = companies;
    this.branches = branches;
    this.tenant = tenant;
    this.audit = audit;
    this.external360 = external360;
    this.objectMapper = objectMapper;
  }

  public List<ClientResponse> search(String q, String plate) {
    List<Client> direct = clients.search(tenant.companyId(), normalizeQuery(q)).stream().toList();
    if (plate == null || plate.isBlank()) {
      return direct.stream().map(this::toClient).toList();
    }
    List<UUID> plateMatches = external360.findClientIdsByPlate(tenant.companyId(), plate.trim().toUpperCase());
    LinkedHashMap<UUID, Client> merged = direct.stream()
        .collect(Collectors.toMap(client -> client.id, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    plateMatches.forEach(id -> clients.findByIdAndEmpresa_Id(id, tenant.companyId()).ifPresent(client -> merged.putIfAbsent(client.id, client)));
    return merged.values().stream().map(this::toClient).toList();
  }

  public Client360Response detail(UUID id) {
    Client client = clientInTenant(id);
    return new Client360Response(
        toClient(client),
        contacts.findByCliente_IdAndEmpresa_IdOrderByPrincipalDescNombreAsc(id, tenant.companyId()).stream().map(this::toContact).toList(),
        documents.findByCliente_IdAndEmpresa_IdOrderByFechaCreacionDesc(id, tenant.companyId()).stream().map(this::toDocument).toList(),
        communications.findByCliente_IdAndEmpresa_IdOrderByFechaHoraDesc(id, tenant.companyId()).stream().map(this::toCommunication).toList(),
        external360.vehicles(id),
        external360.appointments(id),
        external360.workOrders(id),
        external360.invoices(id),
        external360.payments(id),
        external360.portfolio(id));
  }

  @Transactional
  public ClientResponse create(ClientRequest request) {
    Company company = companies.findById(tenant.companyId()).orElseThrow();
    Client client = new Client();
    client.id = UUID.randomUUID();
    client.empresa = company;
    applyClient(client, request);
    client.fechaCreacion = Instant.now();
    client.fechaModificacion = Instant.now();
    Client saved = clients.save(client);
    audit.record("CLIENTES", "CREAR", "clientes", saved.id, null, json(toClient(saved)));
    return toClient(saved);
  }

  @Transactional
  public ClientResponse update(UUID id, ClientRequest request) {
    Client client = clientInTenant(id);
    String before = json(toClient(client));
    applyClient(client, request);
    client.fechaModificacion = Instant.now();
    Client saved = clients.save(client);
    audit.record("CLIENTES", "EDITAR", "clientes", saved.id, before, json(toClient(saved)));
    return toClient(saved);
  }

  @Transactional
  public void deactivate(UUID id) {
    Client client = clientInTenant(id);
    String before = json(toClient(client));
    client.estado = "INACTIVO";
    client.fechaModificacion = Instant.now();
    clients.save(client);
    audit.record("CLIENTES", "ELIMINAR", "clientes", id, before, "{\"estado\":\"INACTIVO\"}");
  }

  @Transactional
  public ContactResponse addContact(UUID clientId, ContactRequest request) {
    Client client = clientInTenant(clientId);
    ClientContact contact = new ClientContact();
    contact.id = UUID.randomUUID();
    contact.empresa = client.empresa;
    contact.cliente = client;
    applyContact(contact, request);
    contact.fechaCreacion = Instant.now();
    contact.fechaModificacion = Instant.now();
    ClientContact saved = contacts.save(contact);
    audit.record("CLIENTES", "CREAR", "cliente_contactos", saved.id, null, json(toContact(saved)));
    return toContact(saved);
  }

  @Transactional
  public DocumentResponse addDocument(UUID clientId, DocumentRequest request) {
    Client client = clientInTenant(clientId);
    ClientDocument document = new ClientDocument();
    document.id = UUID.randomUUID();
    document.empresa = client.empresa;
    document.cliente = client;
    document.tipo = required(request.tipo(), "tipo");
    document.nombreArchivo = required(request.nombreArchivo(), "nombreArchivo");
    document.url = required(request.url(), "url");
    document.notas = request.notas();
    document.estado = defaultValue(request.estado(), "ACTIVO");
    document.fechaCreacion = Instant.now();
    document.fechaModificacion = Instant.now();
    ClientDocument saved = documents.save(document);
    audit.record("CLIENTES", "CREAR", "cliente_documentos", saved.id, null, json(toDocument(saved)));
    return toDocument(saved);
  }

  @Transactional
  public CommunicationResponse addCommunication(UUID clientId, CommunicationRequest request) {
    Client client = clientInTenant(clientId);
    ClientCommunication communication = new ClientCommunication();
    communication.id = UUID.randomUUID();
    communication.empresa = client.empresa;
    communication.cliente = client;
    communication.canal = required(request.canal(), "canal").toUpperCase();
    communication.direccion = request.direccion();
    communication.asunto = request.asunto();
    communication.contenido = request.contenido();
    communication.estado = defaultValue(request.estado(), "REGISTRADA");
    communication.fechaHora = Instant.now();
    ClientCommunication saved = communications.save(communication);
    audit.record("CLIENTES", "CREAR", "cliente_comunicaciones", saved.id, null, json(toCommunication(saved)));
    return toCommunication(saved);
  }

  private void applyClient(Client client, ClientRequest request) {
    client.sucursal = request.sucursalId() == null ? null : branchInTenant(request.sucursalId());
    client.tipoPersona = defaultValue(request.tipoPersona(), "NATURAL").toUpperCase();
    client.tipoIdentificacion = request.tipoIdentificacion();
    client.identificacion = request.identificacion();
    client.nombre = required(request.nombre(), "nombre");
    client.razonSocial = request.razonSocial();
    client.telefonoPrincipal = request.telefonoPrincipal();
    client.telefonoSecundario = request.telefonoSecundario();
    client.whatsapp = request.whatsapp();
    client.email = request.email() == null ? null : request.email().toLowerCase();
    client.direccion = request.direccion();
    client.notas = request.notas();
    client.preferencias = request.preferencias();
    client.estado = defaultValue(request.estado(), "ACTIVO");
  }

  private void applyContact(ClientContact contact, ContactRequest request) {
    contact.nombre = required(request.nombre(), "nombre");
    contact.relacion = request.relacion();
    contact.telefono = request.telefono();
    contact.whatsapp = request.whatsapp();
    contact.email = request.email() == null ? null : request.email().toLowerCase();
    contact.principal = request.principal();
    contact.notas = request.notas();
    contact.estado = defaultValue(request.estado(), "ACTIVO");
  }

  private Client clientInTenant(UUID id) {
    return clients.findByIdAndEmpresa_Id(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Cliente no pertenece a la empresa actual"));
  }

  private Branch branchInTenant(UUID id) {
    tenant.assertBranchAllowed(id);
    return branches.findByIdAndEmpresaId(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Sucursal no pertenece a la empresa actual"));
  }

  private ClientResponse toClient(Client client) {
    return new ClientResponse(client.id, client.empresa.id, client.sucursal == null ? null : client.sucursal.id,
        client.tipoPersona, client.tipoIdentificacion, client.identificacion, client.nombre, client.razonSocial,
        client.telefonoPrincipal, client.telefonoSecundario, client.whatsapp, client.email, client.direccion,
        client.notas, client.preferencias, client.estado, client.fechaCreacion, client.fechaModificacion);
  }

  private ContactResponse toContact(ClientContact contact) {
    return new ContactResponse(contact.id, contact.nombre, contact.relacion, contact.telefono, contact.whatsapp,
        contact.email, contact.principal, contact.notas, contact.estado);
  }

  private DocumentResponse toDocument(ClientDocument document) {
    return new DocumentResponse(document.id, document.tipo, document.nombreArchivo, document.url, document.notas,
        document.estado, document.fechaCreacion);
  }

  private CommunicationResponse toCommunication(ClientCommunication communication) {
    return new CommunicationResponse(communication.id, communication.canal, communication.direccion,
        communication.asunto, communication.contenido, communication.estado, communication.fechaHora);
  }

  private String json(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private String normalizeQuery(String value) {
    return value == null ? "" : value.trim();
  }

  private String required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El campo " + field + " es obligatorio");
    }
    return value.trim();
  }

  private String defaultValue(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
  }
}
