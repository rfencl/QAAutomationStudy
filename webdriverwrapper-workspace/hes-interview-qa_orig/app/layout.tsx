import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import Navbar from "@/components/navbar";
import { cookies } from "next/headers";
import { getAuthUser } from "@/actions/actionsAuth";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Census App",
  description: "Generated QA for testing purpose only.",
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  cookies();
  const authUser = await getAuthUser();

  return (
    <html lang="en">
      <body className={inter.className}>
        <div className="min-h-full">
          <Navbar authUser={authUser} />
          {children}
        </div>
      </body>
    </html>
  );
}
