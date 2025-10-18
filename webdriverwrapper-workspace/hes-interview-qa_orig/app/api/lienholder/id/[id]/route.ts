// /api/lienholder/id/[id]

import { deleteLienholderById, getLienholderById } from "@/actions/actionsHousehold";

export async function GET(request: Request, { params }: { params: { id: string } }) {
  const id: number | undefined = parseInt(params.id) || undefined;
  const { success, lienholder, error, db_error, code } = await getLienholderById(id);
  return Response.json({ success, lienholder, error, db_error }, { status: code });
}

export async function DELETE(request: Request, { params }: { params: { id: string } }) {
  const id: number | undefined = parseInt(params.id) || undefined;
  const { success, error, db_error, code } = await deleteLienholderById(id);
  return Response.json({ success, error, db_error }, { status: code });
}
