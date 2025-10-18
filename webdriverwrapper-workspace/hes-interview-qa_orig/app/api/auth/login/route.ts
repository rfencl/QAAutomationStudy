// /api/auth/login

import { login } from "@/actions/actionsAuth";
import { NextResponse, NextRequest } from "next/server";

export async function POST(request: NextRequest) {
  const { error, success, data } = await login(await request.json());
  if (error) return NextResponse.json({ error, data }, { status: 401 });
  if (success) return NextResponse.json({ success }, { status: 200 });
  return NextResponse.json({ error: "Something went wrong." }, { status: 500 });
}
