import "./globals.css";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Taller 360",
  description: "Administracion integral de talleres automotrices"
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="es">
      <body>{children}</body>
    </html>
  );
}
