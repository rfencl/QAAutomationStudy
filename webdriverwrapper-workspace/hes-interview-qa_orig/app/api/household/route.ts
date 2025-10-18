// /api/household

import { getHouseholdByUserEmail, getHouseholdByUserId, saveHousehold, updateHousehold } from "@/actions/actionsHousehold";

export async function PUT(request: Request) {
  const body = await request.json();
  const { success, household, error, data, db_error, code } = await saveHousehold(body);
  return Response.json({ success, household, error, data, db_error }, { status: code });
}

export async function PATCH(request: Request) {
  const body = await request.json();
  const { success, household, error, data, db_error, code } = await updateHousehold(body);
  return Response.json({ success, household, error, data, db_error }, { status: code });
}

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const id: number | undefined = searchParams.get("id") ? parseInt(searchParams.get("id") as string) : undefined;
  const email: string | undefined = searchParams.get("email") || undefined;
  if (id) {
    const { success, household, error, db_error, code } = await getHouseholdByUserId(id);
    return Response.json({ success, household, error, db_error }, { status: code });
  } else if (email) {
    const { success, household, error, db_error, code } = await getHouseholdByUserEmail(email);
    return Response.json({ success, household, error, db_error }, { status: code });
  } else return Response.json({ error: "At least one query parameter required, id or email" }, { status: 403 });
}
