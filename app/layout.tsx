import "./globals.css";

export const metadata = {
  title: "JoeyPalma AI Helper Center",
  description: "Cyberpunk AI assistant dashboard",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body className="bg-[#02030a] text-white min-h-screen">
        {children}
      </body>
    </html>
  );
}
