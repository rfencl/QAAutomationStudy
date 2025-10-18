import NextAuth from "next-auth";
import { PrismaAdapter } from "@auth/prisma-adapter";
import { db } from "@/lib/db";
import authConfig from "@/auth.config";
import { dbGetUserById } from "@/data/dbUsers";

export const {
  handlers: { GET, POST },
  auth,
  signIn,
  signOut,
  unstable_update,
} = NextAuth({
  pages: {
    signIn: "/auth/login",
    error: "/auth/error",
  },
  events: {
    //   async linkAccount({ user }) {
    //     await db.user.update({
    //       where: { id: user.id },
    //       data: { emailVerified: new Date() },
    //     });
    //   },
  },
  callbacks: {
    async signIn({ user }) {
      const existingUser = user.id ? await dbGetUserById(parseInt(user.id)) : null;
      if (!existingUser) return false;
      return true;
    },
    async session({ token, session }) {
      if (token.sub && session.user) session.user.id = token.sub;
      // if (token.role && session.user) session.user.role = token.role;
      // console.log(JSON.stringify(session, null, 2));
      return session;
    },
    async jwt({ token, trigger, session, account }) {
      // if (!token.sub) return token;
      // const existingUser = await dbGetUserById(parseInt(token.sub as string));
      // if (!existingUser) return token;
      if (trigger === "update" && session) {
        const { name, email, image } = session;
        if (name) token.name = name;
        if (email) token.email = email;
        if (image) token.picture = image;
      }
      // token.role = existingUser.role;
      return token;
    },
  },
  adapter: PrismaAdapter(db),
  session: {
    strategy: "jwt",
    // Seconds - How long until an idle session expires and is no longer valid.
    // 30 * 24 * 60 * 60, // 30 days
    maxAge: 2 * 60 * 60, // 2 hours
    // Seconds - Throttle how frequently to write to database to extend a session.
    // Use it to limit write operations. Set to 0 to always update the database.
    // Note: This option is ignored if using JSON Web Tokens
    // updateAge: 24 * 60 * 60, // 24 hours
  },
  ...authConfig,
});
