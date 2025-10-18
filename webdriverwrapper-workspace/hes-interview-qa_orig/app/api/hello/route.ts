// /api/hello

export async function GET() {
  return Response.json({ text: "Hello" }, { status: 301 });
}
