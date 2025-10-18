// /api/auth/register

import { register } from "@/actions/actionsUser";

export async function POST(request: Request) {
  const { success, user, error, db_error, data } = await register(await request.json());
  if (success && user) return Response.json({ success, user_id: user.id }, { status: 201 });
  else {
    if (db_error) return Response.json({ error, db_error }, { status: 500 });
    else return Response.json({ error, data }, { status: 403 });
  }
}
