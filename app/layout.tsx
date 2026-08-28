import type { Metadata } from "next"

export const metadata: Metadata = {
  title: "Joey AI - Smart AI Chatbot",
  description: "Smart, charismatic AI chatbot companion with secure memory-safe engine",
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
