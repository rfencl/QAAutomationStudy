import Credentials from "next-auth/providers/credentials";
import type { NextAuthConfig, User } from "next-auth";
import { LoginSchema } from "@/schemas";
import { dbGetUserByEmail } from "@/data/dbUsers";
import bcrypt from "bcryptjs";

export default {
  providers: [
    Credentials({
      async authorize(credentials) {
        const validatedFields = LoginSchema.safeParse(credentials);

        if (validatedFields.success) {
          const { email, password } = validatedFields.data;

          const user = await dbGetUserByEmail(email);
          if (!user || !user.password) return null;

          const passwordMatch = await bcrypt.compare(password, user.password);

          if (passwordMatch) return user as unknown as User;
        }
        return null;
      },
    }),
  ],
} satisfies NextAuthConfig;
