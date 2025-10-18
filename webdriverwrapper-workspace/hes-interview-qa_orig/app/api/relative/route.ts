// /api/relative

import { saveRelativeInfo, updateRelativeInfo } from "@/actions/actionsRecord";

export async function PUT(request: Request) {
  const body = await request.json();
  const { success, relative, error, data, db_error, code } = await saveRelativeInfo(body);
  return Response.json({ success, relative, error, data, db_error }, { status: code });
}

export async function PATCH(request: Request) {
  const body = await request.json();
  const { success, relative, error, data, db_error, code } = await updateRelativeInfo(body);
  return Response.json({ success, relative, error, data, db_error }, { status: code });
}
