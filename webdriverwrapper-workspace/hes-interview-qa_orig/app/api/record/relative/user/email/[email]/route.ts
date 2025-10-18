// /api/record/relative/user/email/[email]

import { getRecordsWithRelativesInfoUnderUserEmail } from "@/actions/actionsRecord";

export async function GET(request: Request, { params }: { params: { email: string } }) {
  const email: string | undefined = params.email || undefined;
  const { success, recordsWithRelationship, error, db_error, code } = await getRecordsWithRelativesInfoUnderUserEmail(email);
  return Response.json({ success, recordsWithRelationship, error, db_error }, { status: code });
}
