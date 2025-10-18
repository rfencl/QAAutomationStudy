import { db } from "@/lib/db";
import { HomeType, Household, Lienholder, Ownership, State } from "@prisma/client";

export const dbGetHouseholdById = async (id: number): Promise<{ household?: Household | null; db_error?: string }> => {
  try {
    const household = await db.household.findUnique({
      where: {
        id,
      },
    });
    return { household };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbGetHouseholdByUserId = async (userId: number): Promise<{ household?: Household | null; db_error?: string }> => {
  try {
    const result = await db.user.findUnique({
      where: {
        id: userId,
      },
      select: { household: true },
    });
    if (result && result.household) return { household: result.household };
    else return { household: null };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbSaveHousehold = async ({
  homeType,
  ownership,
  lienholderId = null,
  address1,
  address2,
  city,
  state,
  zip,
}: {
  homeType: HomeType;
  ownership: Ownership;
  lienholderId?: number | null;
  address1: string;
  address2?: string;
  city: string;
  state: State;
  zip: string;
}): Promise<{ household?: Household; db_error?: string }> => {
  try {
    const household = await db.household.create({
      data: { homeType, ownership, lienholderId, address1, address2, city, state, zip },
    });
    return { household };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbUpdateHousehold = async ({
  id,
  homeType,
  ownership,
  lienholderId = null,
  address1,
  address2,
  city,
  state,
  zip,
}: {
  id: number;
  homeType: HomeType;
  ownership: Ownership;
  lienholderId?: number | null;
  address1: string;
  address2?: string;
  city: string;
  state: State;
  zip: string;
}): Promise<{ household?: Household; db_error?: string }> => {
  try {
    const household = await db.household.update({
      where: { id },
      data: { homeType, ownership, lienholderId, address1, address2, city, state, zip },
    });
    return { household };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbSaveLienholder = async (name: string): Promise<{ lienholder?: Lienholder | null; db_error?: string }> => {
  try {
    const lienholder = await db.lienholder.create({
      data: { name },
    });
    return { lienholder };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbGetAllLienholders = async (): Promise<{ lienholders?: Lienholder[] | null; db_error?: string }> => {
  try {
    const lienholders = await db.lienholder.findMany();
    return { lienholders };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbGetLienholderById = async (id: number): Promise<{ lienholder?: Lienholder | null; db_error?: string }> => {
  try {
    const lienholder = await db.lienholder.findUnique({
      where: { id },
    });
    return { lienholder };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbGetLienholderByName = async (name: string): Promise<{ lienholder?: Lienholder | null; db_error?: string }> => {
  try {
    const lienholder = await db.lienholder.findUnique({
      where: { name },
    });
    return { lienholder };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbUpdateLienholder = async ({
  id,
  name,
}: {
  id: number;
  name: string;
}): Promise<{ lienholder?: Lienholder | null; db_error?: string }> => {
  try {
    const lienholder = await db.lienholder.update({
      where: { id },
      data: { name },
    });
    return { lienholder };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbDeleteLienholderById = async (id: number): Promise<{ lienholder?: Lienholder | null; db_error?: string }> => {
  try {
    const lienholder = await db.lienholder.delete({
      where: { id },
    });
    return { lienholder };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbDeleteLienholderByName = async (name: string): Promise<{ lienholder?: Lienholder | null; db_error?: string }> => {
  try {
    const lienholder = await db.lienholder.delete({
      where: { name },
    });
    return { lienholder };
  } catch (e: any) {
    return { db_error: e.message };
  }
};
