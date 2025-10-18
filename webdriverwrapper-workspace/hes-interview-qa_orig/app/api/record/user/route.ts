// /api/record/user

import { getRecordsUnderUserEmail, getRecordsUnderUserId } from "@/actions/actionsRecord";

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const id: number | undefined = searchParams.get("id") ? parseInt(searchParams.get("id") as string) : undefined;
  const email: string | undefined = searchParams.get("email") || undefined;
  if (id) {
    const { success, records, error, db_error, code } = await getRecordsUnderUserId(id);
    return Response.json({ success, records, error, db_error }, { status: code });
  } else if (email) {
    const { success, records, error, db_error, code } = await getRecordsUnderUserEmail(email);
    return Response.json({ success, records, error, db_error }, { status: code });
  } else return Response.json({ error: "At least one query parameter required, id or email" }, { status: 403 });
}
