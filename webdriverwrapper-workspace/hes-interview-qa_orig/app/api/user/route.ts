// /api/user

import { deleteUserById, getUserByEmail, getUserById, updateUser } from "@/actions/actionsUser";

export async function PATCH(request: Request) {
  const { user, success, error, data, db_error, code } = await updateUser(await request.json());
  return Response.json({ success, user, error, data, db_error }, { status: code });
}

export async function DELETE(request: Request) {
  const { success, error, db_error, code } = await deleteUserById((await request.json()).id);
  return Response.json({ success, error, db_error }, { status: code });
}

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const id: number | undefined = searchParams.get("id") ? parseInt(searchParams.get("id") as string) : undefined;
  const email: string | undefined = searchParams.get("email") || undefined;
  if (id) {
    const { success, user, error, code } = await getUserById(id);
    return Response.json({ success, user, error }, { status: code });
  } else if (email) {
    const { success, user, error, code } = await getUserByEmail(email);
    return Response.json({ success, user, error }, { status: code });
  } else return Response.json({ error: "At least one query parameter required, id or email" }, { status: 403 });
}
