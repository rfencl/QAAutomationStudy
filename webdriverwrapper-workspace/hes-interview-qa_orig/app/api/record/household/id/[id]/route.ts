// /api/record/household/id/[id]

import { deleteRecordsUnderHouseholdId, getRecordsUnderHouseholdId } from "@/actions/actionsRecord";

export async function GET(request: Request, { params }: { params: { id: string } }) {
  const id: number | undefined = parseInt(params.id) || undefined;
  const { success, records, error, db_error, code } = await getRecordsUnderHouseholdId(id);
  return Response.json({ success, records, error, db_error }, { status: code });
}

export async function DELETE(request: Request, { params }: { params: { id: string } }) {
  const id: number | undefined = parseInt(params.id) || undefined;
  const { success, error, db_error, code } = await deleteRecordsUnderHouseholdId(id);
  return Response.json({ success, error, db_error }, { status: code });
}
