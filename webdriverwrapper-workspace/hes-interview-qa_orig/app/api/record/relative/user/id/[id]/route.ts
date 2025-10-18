// /api/record/relative/user/id/[id]

import { getRecordsWithRelativesInfoUnderUserId } from "@/actions/actionsRecord";

export async function GET(request: Request, { params }: { params: { id: string } }) {
  const id: number | undefined = parseInt(params.id) || undefined;
  const { success, recordsWithRelationship, error, db_error, code } = await getRecordsWithRelativesInfoUnderUserId(id);
  return Response.json({ success, recordsWithRelationship, error, db_error }, { status: code });
}
