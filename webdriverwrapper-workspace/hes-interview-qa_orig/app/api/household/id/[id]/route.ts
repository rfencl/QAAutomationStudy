// /api/household/id/[id]

import { getHouseholdById } from "@/actions/actionsHousehold";

export async function GET(request: Request, { params }: { params: { id: string } }) {
  const id = parseInt(params.id);
  const { success, household, error, db_error, code } = await getHouseholdById(id);
  return Response.json({ success, household, error, db_error }, { status: code });
}
