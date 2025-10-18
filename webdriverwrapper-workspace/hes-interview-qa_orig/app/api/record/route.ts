// /api/record

import { saveRecord, updateRecord } from "@/actions/actionsRecord";

export async function PUT(request: Request) {
  const body = await request.json();
  const { success, record, error, data, db_error, code } = await saveRecord(body);
  return Response.json({ success, record, error, data, db_error }, { status: code });
}

export async function PATCH(request: Request) {
  const body = await request.json();
  const { success, record, error, data, db_error, code } = await updateRecord(body);
  return Response.json({ success, record, error, data, db_error }, { status: code });
}
