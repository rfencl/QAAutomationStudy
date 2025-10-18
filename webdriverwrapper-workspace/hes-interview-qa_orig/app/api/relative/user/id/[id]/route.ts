// /api/relative/user/id/[id]

import { getRelativesUnderUserId } from "@/actions/actionsRecord";

export async function GET(request: Request, { params }: { params: { id: string } }) {
  const id: number | undefined = parseInt(params.id) || undefined;
  const { success, relatives, error, db_error, code } = await getRelativesUnderUserId(id);
  return Response.json({ success, relatives, error, db_error }, { status: code });
}
