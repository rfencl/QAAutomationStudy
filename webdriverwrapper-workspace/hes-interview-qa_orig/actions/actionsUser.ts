"use server";

import * as z from "zod";
import bcrypt from "bcryptjs";
import { dbDeleteUserById, dbGetUserByEmail, dbGetUserById, dbSaveNewUser, dbUpdatePassword, dbUpdateUser } from "@/data/dbUsers";
import { RegisterUserSchema, UpdatePasswordSchema, UpdateUserSchema } from "@/schemas";
import { User } from "@prisma/client";
import { AuthUser, Obj } from "@/types/types";
import { getAuthUser } from "./actionsAuth";

/**
 * Returns user by given id. Validate if user authorized
 */
export const getUserById = async (userId: number | undefined): Promise<{ success?: string; user?: User; error?: string; code: number }> => {
  if (typeof userId != "number") return { error: "user id is required and must be number", code: 403 };

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (userId != parseInt(authUser.id as string) && authUser.role != "ADMIN")
    return { error: "You don't have permission to get someone's relatives", code: 401 };

  const user = await dbGetUserById(userId);
  if (!user) return { error: `No user by id '${userId}' found.`, code: 404 };

  return { success: "User found.", user, code: 200 };
};

/**
 * Returns user by given email. Validate if user authorized
 */
export const getUserByEmail = async (email: string | undefined): Promise<{ success?: string; user?: User; error?: string; code: number }> => {
  if (typeof email != "string" || !email.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/))
    return { error: "user email is required and must be correct email format", code: 403 };

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (email != authUser.email && authUser.role != "ADMIN") return { error: "You don't have permission to get someone's relatives", code: 401 };

  const user = await dbGetUserByEmail(email);
  if (!user) return { error: `No user with email '${email}' found.`, code: 404 };

  return { success: "User found.", user, code: 200 };
};

/**
 * Register new user. No user authorization
 * @param values is object of { email: string; password: string; name: string; image: string; }
 */
export const register = async (
  values: z.infer<typeof RegisterUserSchema>
): Promise<{ success?: string; user?: User; error?: string; data?: any; db_error?: string; code: number }> => {
  const validatedFields = RegisterUserSchema.safeParse(values);

  console.log(JSON.stringify(validatedFields.error?.errors, null, 2));

  if (!validatedFields.success) {
    return { error: "Invalid fields!", data: validatedFields.error?.errors, code: 401 };
  }

  const { email, password, name, image } = validatedFields.data;

  if (await dbGetUserByEmail(email)) {
    return { error: "Email already in use!", code: 403 };
  }

  const { user, db_error } = await dbSaveNewUser({
    name,
    email,
    password: await bcrypt.hash(password, 10),
    image,
  });

  if (user) return { success: "Successfully registered new user!", user, code: 201 };
  else {
    return { error: "Failed to save new user", db_error, code: 500 };
  }
};

/**
 * Update user data. Validate if user authorized
 * @param values is and object of
 * { id: number, email: email format, password: string min 6, max 20 characters, name: string, image: string url to avatar image }
 */
export const updateUser = async (
  values: z.infer<typeof UpdateUserSchema>
): Promise<{ success?: string; user?: User; error?: string; data?: any; db_error?: string; code: number }> => {
  const validatedFields = UpdateUserSchema.safeParse(values);
  if (!validatedFields.success) return { error: "Invalid fields!", data: validatedFields.error?.errors, code: 403 };

  const { id, email } = validatedFields.data;

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (id != parseInt(authUser.id as string) && authUser.role != "ADMIN")
    return { error: "You have no permission to update someone's user info", code: 401 };

  if (email && authUser?.email != email && (await dbGetUserByEmail(email))) return { error: "Email already in use!", code: 403 };

  const { user, db_error } = await dbUpdateUser(validatedFields.data);

  if (user) return { success: "Successfully updated user!", user, code: 201 };
  else return { error: "Failed to update user", db_error, code: 500 };
};

/**
 * Deleting user by id. If no id provided returns error message. Validates if user authorized.
 * @param id number
 */
export const deleteUserById = async (id: number | undefined): Promise<{ success?: string; error?: string; db_error?: string; code: number }> => {
  if (typeof id != "number") return { error: "parameter id for user's id is required and must be a number.", code: 403 };

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (id != parseInt(authUser.id as string) && authUser.role != "ADMIN")
    return { error: "You don't have permission to delete somebody's user.", code: 403 };

  const userExist = await dbGetUserById(id);
  if (!userExist) return { error: `no user found by id '${id}'.`, code: 404 };

  const { user, db_error } = await dbDeleteUserById(id);
  if (!user || db_error) return { error: `Failed to delete user by id '${id}'.`, db_error, code: 500 };
  return { success: `Successfully deleted user by id '${id}'.`, code: 201 };
};

/**
 * Update password for user by id. Can be perform by user by matching old password. \
 * ADMIN user can update password without providing old password. Validate id user authorized
 * @param values is and object of { id: number, oldPassword: string | null, newPassword: string }
 */
export const updatePassword = async (
  values: z.infer<typeof UpdatePasswordSchema>
): Promise<{ success?: string; user?: User; error?: string; data?: any; db_error?: string; code: number }> => {
  const validatedFields = UpdatePasswordSchema.safeParse(values);
  if (!validatedFields.success) return { error: "Invalid fields!", data: validatedFields.error?.errors, code: 403 };

  const { id, oldPassword, newPassword } = validatedFields.data;

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  const isAdmin: boolean = authUser.role != "ADMIN";

  if (id != parseInt(authUser?.id as string) && !isAdmin) return { error: "You have no permission to update someone's password", code: 401 };

  let dbUser = await dbGetUserById(id);

  if (!dbUser) return { error: `No user found by id: '${id}'`, code: 404 };

  if (!isAdmin) {
    if (!oldPassword) return { error: "oldPassword is required.", code: 401 };
    const passwordMatch = await bcrypt.compare(oldPassword, dbUser.password);
    if (!passwordMatch) return { error: "Old password doesn't match!", code: 401 };
  }

  const { user, db_error } = await dbUpdatePassword({
    id,
    password: await bcrypt.hash(newPassword, 10),
  });

  if (user) return { success: "Successfully updated password!", user, code: 201 };
  else return { error: "Failed to update password!", db_error, code: 500 };
};
