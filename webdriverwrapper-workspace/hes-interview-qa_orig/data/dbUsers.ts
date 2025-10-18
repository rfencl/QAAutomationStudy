import { db } from "@/lib/db";
import { User, UserRole } from "@prisma/client";

export const dbGetUserByEmail = async (email: string): Promise<User | null> => {
  try {
    const user = await db.user.findUnique({ where: { email } });
    return user;
  } catch {
    return null;
  }
};

export const dbGetUserById = async (id: number): Promise<User | null> => {
  try {
    const user = await db.user.findUnique({ where: { id } });
    return user;
  } catch {
    return null;
  }
};

export const dbGetUsersByRecordId = async (id: number): Promise<{ users?: User[] | null; db_error?: string }> => {
  try {
    const users: User[] | null = await db.user.findMany({
      where: {
        household: {
          Records: {
            some: {
              id,
            },
          },
        },
      },
    });
    return { users };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbGetUsersByHouseholdId = async (householdId: number): Promise<{ users?: User[] | null; db_error?: string }> => {
  try {
    const users: User[] | null = await db.user.findMany({
      where: {
        householdId,
      },
    });
    return { users };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbSaveNewUser = async ({
  name,
  email,
  password,
  image,
  role,
  householdId,
}: {
  name: string;
  email: string;
  password: string;
  image: string;
  role?: UserRole;
  householdId?: number;
}): Promise<{ user?: User; db_error?: string }> => {
  try {
    const user = await db.user.create({
      data: {
        name,
        email,
        password,
        image,
        role,
        householdId,
      },
    });
    return { user };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbUpdateUser = async ({
  id,
  name,
  email,
  image,
  role,
  householdId,
}: {
  id: number;
  name?: string;
  email?: string;
  image?: string;
  role?: UserRole;
  householdId?: number;
}): Promise<{ user?: User; db_error?: string }> => {
  try {
    const user = await db.user.update({
      where: { id },
      data: {
        name,
        email,
        image,
        householdId,
        role,
      },
    });
    return { user };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbDeleteUserById = async (id: number): Promise<{ user?: User; db_error?: string }> => {
  try {
    const user = await db.user.delete({ where: { id } });
    return { user };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbUpdatePassword = async ({ id, password }: { id: number; password: string }): Promise<{ user?: User; db_error?: string }> => {
  try {
    const user = await db.user.update({
      where: { id },
      data: {
        password,
      },
    });
    return { user };
  } catch (e: any) {
    return { db_error: e.message };
  }
};
