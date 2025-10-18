// /api/user/id/[id]

import { getUserById } from "@/actions/actionsUser";

export async function GET(request: Request, { params }: { params: { id: string } }) {
  const id: number | undefined = params.id ? parseInt(params.id as string) : undefined;
  const { success, user, error, code } = await getUserById(id);
  return Response.json({ success, user, error }, { status: code });
}
