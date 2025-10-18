// /api/auth/logout

import { signOut } from "@/auth";
import { AuthError } from "next-auth";
import { NextResponse, NextRequest } from "next/server";

export async function POST(request: NextRequest) {
  try {
    await signOut({ redirect: false });
  } catch (error) {
    if (error instanceof AuthError) {
      switch (error.type) {
        case "CredentialsSignin":
          return NextResponse.json({ error: "Invalid credentials." }, { status: 401 });
        default:
          return NextResponse.json({ error: "Something went wrong." }, { status: 500 });
      }
    }
    throw error;
  }
  return NextResponse.json({ success: "Successful log out." }, { status: 200 });
}
