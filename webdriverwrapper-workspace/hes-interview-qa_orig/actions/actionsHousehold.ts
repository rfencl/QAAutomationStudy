"use server";

import { z } from "zod";
import { dbGetUserByEmail, dbGetUserById, dbUpdateUser } from "@/data/dbUsers";
import {
  dbDeleteLienholderById,
  dbDeleteLienholderByName,
  dbGetAllLienholders,
  dbGetHouseholdById,
  dbGetLienholderById,
  dbGetLienholderByName,
  dbSaveHousehold,
  dbSaveLienholder,
  dbUpdateHousehold,
  dbUpdateLienholder,
} from "@/data/dbHousehold";
import { Household, Lienholder, User } from "@prisma/client";
import { HouseholdSchema, UpdateHouseholdSchema } from "@/schemas";
import { AuthUser } from "@/types/types";
import { getAuthUser } from "./actionsAuth";

/**
 * Returns Household for given userId. Validates if user authorized.
 * @param userId number
 */
export const getHouseholdByUserId = async (
  userId: number
): Promise<{ success?: string; household?: Household; error?: string; db_error?: string; code: number }> => {
  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (parseInt(authUser.id as string) != userId && authUser.role !== "ADMIN")
    return { error: "You don't have permission to ged someone's household", code: 401 };

  const user: User | null = await dbGetUserById(userId);
  if (!user) return { error: `User by id '${userId}' doesn't exist.`, code: 404 };
  if (!user.householdId) return { error: `User by id '${userId}' doesn't have any household saved.`, code: 404 };

  const { household, db_error } = await dbGetHouseholdById(user.householdId);
  if (db_error) return { error: `Failed to get household for user by id '${userId}'.`, db_error, code: 500 };
  if (!household) return { error: `No household found for user by id '${userId}'.`, code: 404 };
  return { success: `Household found for user by id '${userId}'`, household, code: 200 };
};

/**
 * Returns Household for given user email. Validates if user authorized.
 * @param email string
 */
export const getHouseholdByUserEmail = async (
  email: string | undefined
): Promise<{ success?: string; household?: Household; error?: string; db_error?: string; code: number }> => {
  if (typeof email != "string" || !email.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/))
    return { error: "user email is required and must be email format", code: 403 };

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (authUser.email != email && authUser.role !== "ADMIN") return { error: "You don't have permission to ged someone's household", code: 401 };

  const user: User | null = await dbGetUserByEmail(email);
  if (!user) return { error: `User with email '${email}' doesn't exist.`, code: 404 };
  if (!user.householdId) return { error: `User with email '${email}' doesn't have any household saved.`, code: 404 };

  const { household, db_error } = await dbGetHouseholdById(user.householdId);
  if (db_error) return { error: `Failed to get household for user with email '${email}'.`, db_error, code: 500 };
  if (!household) return { error: `No household found for user with email '${email}'.`, code: 404 };
  return { success: `Household found for user with email '${email}'`, household, code: 200 };
};

/**
 * Returns Household by householdId. Validate if user authorized
 * @param householdId number
 */
export const getHouseholdById = async (
  householdId: number
): Promise<{ success?: string; household?: Household; error?: string; db_error?: string; code: number }> => {
  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  const user: User | null = await dbGetUserById(parseInt(authUser.id as string));
  if (user?.householdId !== householdId && authUser.role !== "ADMIN")
    return { error: "You don't have permission to get someone's household", code: 401 };

  const { household, db_error } = await dbGetHouseholdById(householdId);
  if (db_error) return { error: `Failed to get household by id '${householdId}'.`, db_error, code: 500 };
  if (!household) return { error: `No household found by household id '${householdId}'.`, code: 404 };
  return { success: `Household found by id '${householdId}'`, household, code: 200 };
};

/**
 * Save household and assign used by userId to saved household by householdId. Validate if user authorized
 * @param values presented as object of {  userId: number, homeType: "HOUSE" | "APARTMENT" | "MOBILE_HOME" | "SHELTER",
 *   ownership: "MORTGAGE" | "OWN" | "RENT" | "FREE_LIVING", lienholderId: number | null,
 *   address1: string, address2: string | undefined, city: string,
 *   state: state code 2 letters, zip: string /^\d{5}$/ 5 digits }
 */
export const saveHousehold = async (
  values: z.infer<typeof HouseholdSchema>
): Promise<{ success?: string; household?: Household; error?: string; db_error?: string; data?: any; code: number }> => {
  const validatedFields = HouseholdSchema.safeParse(values);
  console.log(JSON.stringify(validatedFields.error?.errors, null, 2));
  if (!validatedFields.success) return { error: "Invalid fields!", data: validatedFields.error?.errors, code: 403 };

  const { userId } = validatedFields.data;

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (userId != parseInt(authUser?.id as string) && authUser.role != "ADMIN")
    return { error: "You have no permission to save someone's household.", code: 403 };

  let dbUser = await dbGetUserById(userId);
  if (!dbUser) return { error: `Couldn't find user by id: '${userId}'`, code: 404 };

  const { household, db_error } = await dbSaveHousehold(validatedFields.data);
  if (db_error || !household) return { error: "Failed to save household.", db_error, code: 500 };

  const result = await dbUpdateUser({ id: userId, householdId: household.id });
  if (result.db_error) return { error: `Couldn't assign user by id: '${userId}' to household by householdId '${household.id}'.`, code: 500 };
  return { success: "Household saved successfully.", household, code: 201 };
};

/**
 * Update household by id. Validate if user authorized
 * @param values presented as object of { id: number, userId: number, homeType: "HOUSE" | "APARTMENT" | "MOBILE_HOME" | "SHELTER",
 *   ownership: "MORTGAGE" | "OWN" | "RENT" | "FREE_LIVING", lienholderId: number | null,
 *   address1: string, address2: string | undefined, city: string,
 *   state: state code 2 letters, zip: string /^\d{5}$/ 5 digits }
 */
export const updateHousehold = async (
  values: z.infer<typeof UpdateHouseholdSchema>
): Promise<{ success?: string; household?: Household; error?: string; db_error?: string; data?: any; code: number }> => {
  const validatedFields = UpdateHouseholdSchema.safeParse(values);
  console.log(JSON.stringify(validatedFields.error?.errors, null, 2));
  if (!validatedFields.success) return { error: "Invalid fields!", data: validatedFields.error?.errors, code: 403 };

  const { id } = validatedFields.data;

  const result1 = await getHouseholdById(id);
  if (!result1.household) {
    if (result1.code == 401) {
      result1.error = "You don't have permission to update someone's household";
      return result1;
    } else return result1;
  }

  const { household, db_error } = await dbUpdateHousehold(validatedFields.data);
  if (db_error || !household) return { error: "Failed to update household.", db_error, code: 500 };
  return { success: "Household saved successfully.", household, code: 201 };
};

/**
 * Returns all lienholders in array. No user authorization
 */
export const getAllLienholders = async (): Promise<{
  success?: string;
  lienholders?: Lienholder[];
  error?: string;
  db_error?: string;
  code: number;
}> => {
  const { lienholders, db_error } = await dbGetAllLienholders();
  if (!lienholders?.length || db_error) return { error: "No lienholders found.", db_error, code: 404 };
  return { success: "Lienholders found.", lienholders, code: 200 };
};

/**
 * Save new lienholder if does not exist already. no user authorization
 * @param name string lienholder name
 */
export const saveLienholder = async (
  name: string | undefined
): Promise<{
  success?: string;
  lienholder?: Lienholder;
  error?: string;
  db_error?: string;
  code: number;
}> => {
  if (typeof name != "string" || !name.length) return { error: "Lienholder name required.", code: 403 };
  const exist = await dbGetLienholderByName(name);
  if (exist.lienholder) return { error: `Lienholder by name '${name}" already exist.`, lienholder: exist.lienholder, code: 403 };
  const { lienholder, db_error } = await dbSaveLienholder(name);
  if (!lienholder || db_error) return { error: "Failed to save lienholders.", db_error, code: 500 };
  return { success: "Successfully saved lienholder.", lienholder, code: 201 };
};

/**
 * Update lienholder name if new name does not exist already. Only ADMIN user is authorized
 * @param param0 presented as object of { id: number; name: string; }
 */
export const updateLienholder = async ({
  id,
  name,
}: {
  id: number;
  name: string;
}): Promise<{
  success?: string;
  lienholder?: Lienholder;
  error?: string;
  db_error?: string;
  code: number;
}> => {
  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (authUser.role != "ADMIN") return { error: "You don't have permission to update lienholder", code: 401 };

  if (!name.length) return { error: "Lienholder name required.", code: 403 };

  const result1 = await dbGetLienholderByName(name);
  if (result1.lienholder) return { error: `Lienholder by name '${name}" already exist.`, lienholder: result1.lienholder, code: 403 };

  const { lienholder, db_error } = await dbUpdateLienholder({ id, name });
  if (!lienholder || db_error) return { error: "Failed to update lienholders.", db_error, code: 500 };
  return { success: "Successfully updated lienholder.", lienholder, code: 201 };
};

/**
 * Returns lienholder by id. No user authorization
 * @param id number
 */
export const getLienholderById = async (
  id: number | undefined
): Promise<{
  success?: string;
  lienholder?: Lienholder;
  error?: string;
  db_error?: string;
  code: number;
}> => {
  if (typeof id != "number") return { error: "id is required as number", code: 403 };
  const { lienholder, db_error } = await dbGetLienholderById(id);
  if (db_error) return { error: `Failed to find lienholder by id '${id}'.`, db_error, code: 500 };
  if (!lienholder) return { error: `No lienholder found by id '${id}'.`, db_error, code: 404 };
  return { success: "Successfully found lienholder.", lienholder, code: 200 };
};

/**
 * Deleting lienholder by id if exist. Only ADMIN user is authorized.
 * @param id number
 */
export const deleteLienholderById = async (
  id: number | undefined
): Promise<{
  success?: string;
  error?: string;
  db_error?: string;
  code: number;
}> => {
  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (authUser.role != "ADMIN") return { error: "You don't have permission to delete lienholder. Please contact ADMIN.", code: 401 };

  if (typeof id != "number") return { error: "id is required", code: 403 };

  const result1 = await dbGetLienholderById(id);
  if (!result1.lienholder) return { error: `No lienholder found by id '${id}'`, code: 404 };

  const { lienholder, db_error } = await dbDeleteLienholderById(id);
  if (db_error) return { error: "Something went wrong", db_error, code: 500 };
  console.log(lienholder);

  return { success: "lienholder deleted successfully.", code: 201 };
};

/**
 * Deleting lienholder by name if exist. Only ADMIN user is authorized.
 * @param name string
 */
export const deleteLienholderByName = async (
  name: string | undefined
): Promise<{
  success?: string;
  error?: string;
  db_error?: string;
  code: number;
}> => {
  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (authUser.role != "ADMIN") return { error: "You don't have permission to delete lienholder. Please contact ADMIN.", code: 401 };

  if (typeof name != "string") return { error: "name is required", code: 403 };

  const result1 = await dbGetLienholderByName(name);
  if (!result1.lienholder) return { error: `No lienholder found by name '${name}'`, code: 404 };

  const { lienholder, db_error } = await dbDeleteLienholderByName(name);
  if (db_error) return { error: "Something went wrong", db_error, code: 500 };
  console.log(lienholder);

  return { success: "lienholder deleted successfully.", code: 201 };
};
