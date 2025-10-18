// /api/lienholder

import { deleteLienholderByName, getAllLienholders, saveLienholder, updateLienholder } from "@/actions/actionsHousehold";

export async function GET(request: Request) {
  const { success, lienholders, error, db_error, code } = await getAllLienholders();
  return Response.json({ success, lienholders, error, db_error }, { status: code });
}

export async function PUT(request: Request) {
  const name: string | undefined = (await request.json()).name;
  const { success, lienholder, error, db_error, code } = await saveLienholder(name);
  return Response.json({ success, lienholder, error, db_error }, { status: code });
}

export async function PATCH(request: Request) {
  const body = await request.json();
  const { success, lienholder, error, db_error, code } = await updateLienholder(body);
  return Response.json({ success, lienholder, error, db_error }, { status: code });
}

export async function DELETE(request: Request) {
  const name: string | undefined = (await request.json()).name;
  const { success, error, db_error, code } = await deleteLienholderByName(name);
  return Response.json({ success, error, db_error }, { status: code });
}
