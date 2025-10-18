// /api/household/user/id/[id]

import { getHouseholdByUserId } from "@/actions/actionsHousehold";

export async function GET(request: Request, { params }: { params: { id: string } }) {
  const id = parseInt(params.id);
  const { success, household, error, db_error, code } = await getHouseholdByUserId(id);
  return Response.json({ success, household, error, db_error }, { status: code });
}
