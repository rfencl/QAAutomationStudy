import { updatePassword } from "@/actions/actionsUser";

export async function POST(request: Request) {
  const { success, error, data, db_error, code } = await updatePassword(await request.json());
  return Response.json({ success, error, data, db_error }, { status: code });
}
