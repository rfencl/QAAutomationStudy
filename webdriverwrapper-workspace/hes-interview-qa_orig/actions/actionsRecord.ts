"use server";

import {
  dbDeleteRecordById,
  dbDeleteRecordsUnderHouseholdId,
  dbGetRecordById,
  dbGetRecordsUnderHouseholdId,
  dbGetRelationshipsUnderUserEmail,
  dbGetRelationshipsUnderUserId,
  dbSaveRecord,
  dbSaveRelative,
  dbUpdateRecord,
  dbUpdateRelative,
} from "@/data/dbRecord";
import { Record, Relative, User } from "@prisma/client";
import { getHouseholdById, getHouseholdByUserEmail, getHouseholdByUserId } from "./actionsHousehold";
import { RecordSchema, RecordWithRelationshipSchema, UpdateRecordSchema, UpdateRecordWithRelationshipSchema } from "@/schemas";
import { z } from "zod";
import { dbGetUserById, dbGetUsersByHouseholdId, dbGetUsersByRecordId } from "@/data/dbUsers";
import { dbGetHouseholdById, dbGetHouseholdByUserId } from "@/data/dbHousehold";
import { getAuthUser } from "./actionsAuth";
import { AuthUser, RecordWithRelationship } from "@/types/types";

/**
 * Returns records under household by householdId. Validates if user authorized
 * @param householdId number
 */
export const getRecordsUnderHouseholdId = async (
  householdId: number | undefined
): Promise<{ success?: string; records?: Record[]; error?: string; db_error?: string; code: number }> => {
  if (typeof householdId != "number") return { error: "household id is required and must be number", code: 403 };

  const result = await getHouseholdById(householdId); // Validates if user authorized
  if (!result.household) return result;

  const { records, db_error } = await dbGetRecordsUnderHouseholdId(householdId);
  if (db_error) return { error: `Failed to get records for household id '${householdId}'.`, db_error, code: 500 };
  if (!records || !records.length) return { error: `No records found for household id '${householdId}'.`, code: 404 };

  return { success: `Records found for household id '${householdId}'`, records, code: 200 };
};

/**
 * Returns records under user user by userId. Validates if user authorized
 * @param userId number
 */
export const getRecordsUnderUserId = async (
  userId: number | undefined
): Promise<{ success?: string; records?: Record[]; error?: string; data?: any; db_error?: string; code: number }> => {
  if (typeof userId != "number") return { error: "user id is required and must be number", code: 403 };

  const result = await getHouseholdByUserId(userId); // Validates if user authorized
  if (!result.household) return result;

  const { records, db_error } = await dbGetRecordsUnderHouseholdId(result.household.id);
  if (db_error) return { error: `Failed to get records for user by id '${userId}'.`, db_error, code: 500 };
  if (!records || !records.length) return { error: `No records found for user by id '${userId}'.`, code: 404 };

  return { success: `Records found for user by id '${userId}'`, records, code: 200 };
};

/**
 * Returns records under user user by email. Validates if user authorized
 * @param email string
 */
export const getRecordsUnderUserEmail = async (
  email: string | undefined
): Promise<{ success?: string; records?: Record[]; error?: string; data?: any; db_error?: string; code: number }> => {
  if (typeof email != "string" || !email.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/))
    return { error: "user email is required and must be correct email format", code: 403 };

  const result = await getHouseholdByUserEmail(email); // Validates if user authorized
  if (!result.household) return result;

  const { records, db_error } = await dbGetRecordsUnderHouseholdId(result.household.id);
  if (db_error) return { error: `Failed to get records for user with email '${email}'.`, db_error, code: 500 };
  if (!records || !records.length) return { error: `No records found for user with email '${email}'.`, code: 404 };

  return { success: `Records found for user with email '${email}'`, records, code: 200 };
};

/**
 * Returns record by id. Validates if user authorized
 * @param recordId number
 */
export const getRecordById = async (
  recordId: number | undefined
): Promise<{ success?: string; record?: Record; error?: string; data?: any; db_error?: string; code: number }> => {
  if (typeof recordId != "number") return { error: "record id is required and must be number", code: 403 };

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (authUser.role != "ADMIN") {
    const user = dbGetUserById(parseInt(authUser.id as string));
    if (!user) return { error: "No user found under your session. Your user possibly deleted.", code: 500 };

    const result1 = await dbGetHouseholdByUserId(parseInt(authUser.id as string));
    if (!result1.household || result1.db_error) return { error: "No household found under your session.", code: 404 };

    const result2 = await dbGetRecordsUnderHouseholdId(result1.household.id);
    const record = result2.records ? result2.records.find((r) => r.id == recordId) : null;
    if (record == undefined || record == null) return { error: "You have no permission to get someone's records", code: 403 };
    return { success: "Record found.", record, code: 200 };
  } else {
    const { record, db_error } = await dbGetRecordById(recordId);
    if (db_error) return { error: "Failed to find record.", code: 500 };
    if (!record) return { error: `No record found by id '${record}'.`, code: 404 };
    return { success: "Record found.", record, code: 200 };
  }
};

/**
 * Saves record under household by householdId provided in arguments object. Validates if user authorized
 * @param values is an object of { firstName: string, lastName: string, dob: "MM/dd/yyyy",
 *   gender: "MALE" | "FEMALE", telephone: number | undefined, householdId: number,
 *   hispanic: "NO" | "MEXICAN" | "PUERTO_RICAN" | "CUBAN" | "OTHER" | "NO_ANSWER", hispanicOther: string | undefined
 *   race: "WHITE" | "BLACK" | "CHINESE" | "FILIPINO" | "ASIAN_INDIAN" | \
 *         "VIETNAMESE" | "KOREAN" | "JAPANESE" | "OTHER_ASIAN" | "NATIVE_HAWAIIAN" | \
 *         "SAMOAN" | "CHAMORRO" | "OTHER_PACIFIC" | "OTHER" | "NO_ANSWER",
 *   raceOther: string | undefined,
 *   otherStay: "NO" | "COLLEGE" | "MILITARY_ASSIGNMENT" | "JOB_OR_BUSINESS" | "NURSING_HOME" | \
 *              "WITH_PARENT_OR_OTHER_RELATIVE" | "SEASONAL_OR_SECOND_RESIDENT" | \
 *              "JAIL_OR_PRISON" | "OTHER", }
 */
export const saveRecord = async (
  values: z.infer<typeof RecordSchema>
): Promise<{ success?: string; record?: Record; error?: string; data?: any; db_error?: string; code: number }> => {
  const validatedFields = RecordSchema.safeParse(values);
  console.log(JSON.stringify(validatedFields.error?.errors, null, 2));
  if (!validatedFields.success) return { error: "Invalid fields!", data: validatedFields.error?.errors, code: 403 };

  const { firstName, lastName, dob, gender, telephone, householdId, hispanic, hispanicOther, race, raceOther, otherStay } = validatedFields.data;

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  const result1 = await dbGetUsersByHouseholdId(householdId);
  console.log(result1);
  const user: User | undefined = result1.users?.find((u) => u.id == parseInt(authUser.id as string));
  console.log(user);
  if (!user && authUser.role != "ADMIN") return { error: "You don't have permission to save record under someone's household.", code: 401 };

  const { record, db_error } = await dbSaveRecord({
    firstName,
    lastName,
    dob: new Date(dob),
    gender,
    telephone: telephone || null,
    householdId,
    hispanic,
    hispanicOther: hispanicOther && hispanicOther.length > 0 ? hispanicOther : null,
    race,
    raceOther: raceOther && raceOther.length > 0 ? raceOther : null,
    otherStay,
  });
  if (!record || db_error) return { error: "Failed to save new record.", db_error, code: 500 };

  return { success: "Successfully saved new record.", record, code: 201 };
};

/**
 * Update record by id under household by householdId. Validates if user authorized
 * @param values is object of { id: number, firstName: string, lastName: string,
 *   dob: "MM/dd/yyyy", gender: "MALE" | "FEMALE", telephone: number | undefined, householdId: number,
 *   hispanic: "NO" | "MEXICAN" | "PUERTO_RICAN" | "CUBAN" | "OTHER" | "NO_ANSWER",
 *   hispanicOther: string | undefined
 *   race: "WHITE" | "BLACK" | "CHINESE" | "FILIPINO" | "ASIAN_INDIAN" | \
 *         "VIETNAMESE" | "KOREAN" | "JAPANESE" | "OTHER_ASIAN" | "NATIVE_HAWAIIAN" | \
 *         "SAMOAN" | "CHAMORRO" | "OTHER_PACIFIC" | "OTHER" | "NO_ANSWER",
 *   raceOther: string | undefined,
 *   otherStay: "NO" | "COLLEGE" | "MILITARY_ASSIGNMENT" | "JOB_OR_BUSINESS" | "NURSING_HOME" | \
 *              "WITH_PARENT_OR_OTHER_RELATIVE" | "SEASONAL_OR_SECOND_RESIDENT" | \
 *              "JAIL_OR_PRISON" | "OTHER", }
 */
export const updateRecord = async (
  values: z.infer<typeof UpdateRecordSchema>
): Promise<{ success?: string; record?: Record; error?: string; data?: any; db_error?: string; code: number }> => {
  const validatedFields = UpdateRecordSchema.safeParse(values);
  console.log(JSON.stringify(validatedFields.error?.errors, null, 2));
  if (!validatedFields.success) return { error: "Invalid fields!", data: validatedFields.error?.errors, code: 403 };

  const { id, firstName, lastName, dob, gender, telephone, householdId, hispanic, hispanicOther, race, raceOther, otherStay } = validatedFields.data;

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  const result1 = await dbGetUsersByHouseholdId(householdId);
  const user: User | undefined = result1.users?.find((u) => u.id == parseInt(authUser.id as string));
  if (!user || authUser.role != "ADMIN") return { error: "You have no permission to update record under someone's household.", code: 401 };

  const result2 = await dbGetHouseholdById(householdId);
  if (!result2.household) return { error: `No household found by householdId '${householdId}'.`, code: 404 };

  const { record, db_error } = await dbUpdateRecord({
    id,
    firstName,
    lastName,
    dob: new Date(dob),
    gender,
    telephone: telephone || null,
    householdId,
    hispanic,
    hispanicOther: hispanicOther && hispanicOther.length > 0 ? hispanicOther : null,
    race,
    raceOther: raceOther && raceOther.length > 0 ? raceOther : null,
    otherStay,
  });
  if (!record || db_error) return { error: "Failed to update record.", db_error, code: 500 };

  return { success: "Successfully updated record.", record, code: 201 };
};

/**
 * Delete record by id. Validates if user authorized
 * @param recordId number
 */
export const deleteRecordById = async (
  recordId: number | undefined
): Promise<{ success?: string; error?: string; db_error?: string; code: number }> => {
  if (typeof recordId != "number") return { error: "record id is required and must be number", code: 403 };

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  const isAdmin: boolean = authUser.role == "ADMIN";
  const result1 = await dbGetRecordById(recordId);
  if (!result1.record || result1.db_error) {
    // if current user is not connected to record and is not ADMIN then we avoiding giving any information
    if (isAdmin) return { error: `Record by id '${recordId}' doesn't exist.`, db_error: result1.db_error, code: 404 };
    else return { error: "You have no permission to delete record under someone's household.", code: 401 };
  }

  const result2 = await dbGetUsersByRecordId(recordId);
  const user = result2.users?.find((u) => u.id == parseInt(authUser.id as string));
  if (!user && !isAdmin) return { error: "You have no permission to delete record under someone's household.", code: 401 };

  // if record in under user's household or user is admin then record is allowed to be deleted
  const { record, db_error } = await dbDeleteRecordById(recordId);
  if (!record || db_error) return { error: "Failed to delete record.", db_error, code: 500 };
  console.log(record);

  return { success: "Successfully deleted record.", code: 201 };
};

/**
 * Delete all records under household by householdId. Validates if user authorized
 * @param householdId number
 */
export const deleteRecordsUnderHouseholdId = async (
  householdId: number | undefined
): Promise<{ success?: string; error?: string; db_error?: string; code: number }> => {
  if (typeof householdId != "number") return { error: "household id is required and must be number", code: 403 };

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  const isAdmin: boolean = authUser.role == "ADMIN";

  const result1 = await dbGetHouseholdById(householdId);
  if (!result1.household) {
    // if current user is not connected to record and is not ADMIN then we avoiding giving any information
    if (isAdmin) return { error: `Household by id '${householdId}' doesn't exist.`, db_error: result1.db_error, code: 404 };
    else return { error: "You have no permission to delete records under someone's household.", code: 401 };
  }

  const result2 = await dbGetUserById(parseInt(authUser.id as string));
  if (result1.household.id != result2?.householdId && !isAdmin)
    return { error: "You have no permission to delete records under someone's household.", code: 401 };

  const { db_error } = await dbDeleteRecordsUnderHouseholdId(householdId);
  if (db_error) return { error: "Failed to delete records.", db_error, code: 500 };

  return { success: "Successfully deleted records.", code: 201 };
};

/**
 * Returns relatives information under user by id. Validates if user authorized
 * @param userId number
 */
export const getRelativesUnderUserId = async (
  userId: number | undefined
): Promise<{ success?: string; relatives?: Relative[]; error?: string; db_error?: string; code: number }> => {
  if (typeof userId != "number") return { error: "user id is required and must be number", code: 403 };

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (userId != parseInt(authUser.id as string) && authUser.role != "ADMIN")
    return { error: "You don't have permission to get someone's relatives", code: 401 };

  const { relatives, db_error } = await dbGetRelationshipsUnderUserId(userId);
  if (db_error) return { error: `Failed to get relative information under userId '${userId}'.`, code: 500 };
  if (!relatives || !relatives.length) return { error: `No relative data found under user by id '${userId}'.`, code: 404 };
  return { success: "Relatives information found.", relatives, code: 200 };
};

/**
 * Returns relatives information under user by email. Validates if user authorized
 * @param userId number
 */
export const getRelativesUnderUserEmail = async (
  email: string | undefined
): Promise<{ success?: string; relatives?: Relative[]; error?: string; db_error?: string; code: number }> => {
  if (typeof email != "string" || !email.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/))
    return { error: "user email is required and must be email format", code: 403 };

  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (email != authUser.email && authUser.role != "ADMIN") return { error: "You don't have permission to get someone's relatives", code: 401 };

  const { relatives, db_error } = await dbGetRelationshipsUnderUserEmail(email);
  if (db_error) return { error: `Failed to get relative information for user with email '${email}'.`, code: 500 };
  if (!relatives || !relatives.length) return { error: `No relative data found for user with email '${email}'.`, code: 404 };
  return { success: "Relatives information found.", relatives, code: 200 };
};

/**
 * Saves new relative information. Validates if user authorized
 * @param newRelative is object of { userId: number;
 *   relationship: "SELF" | "SPOUSE" | "PARTNER" | "BIOLOGICAL_CHILD" | "ADOPTED_CHILD" | \
 *                 "STEP_CHILD" | "COSINE" | "PARENT" | "GRANDCHILD" | "GRANDPARENT" | \
 *                 "OTHER_RELATIVE" | "OTHER_NON_RELATIVE" | "ROOMMATE_HOUSEMATE";
 *   recordId: number; }
 */
export const saveRelativeInfo = async (
  newRelative: Omit<Relative, "id">
): Promise<{ success?: string; relative?: Relative; error?: string; data?: any; db_error?: string; code: number }> => {
  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (newRelative.userId != parseInt(authUser.id as string) && authUser.role != "ADMIN")
    return { error: "You don't have permission to save to someone's relatives", code: 401 };

  const { relative, db_error } = await dbSaveRelative(newRelative);
  if (!relative || db_error) return { error: "Failed to save new relative info", code: 500 };
  return { success: "Saved new relative info.", relative, code: 201 };
};

/**
 * Updates existing relative information. Validates if user authorized
 * @param newRelative is object of { id: number; userId: number;
 *   relationship: "SELF" | "SPOUSE" | "PARTNER" | "BIOLOGICAL_CHILD" | "ADOPTED_CHILD" | \
 *                 "STEP_CHILD" | "COSINE" | "PARENT" | "GRANDCHILD" | "GRANDPARENT" | \
 *                 "OTHER_RELATIVE" | "OTHER_NON_RELATIVE" | "ROOMMATE_HOUSEMATE";
 *   recordId: number; }
 */
export const updateRelativeInfo = async (
  newRelative: Relative
): Promise<{ success?: string; relative?: Relative; error?: string; data?: any; db_error?: string; code: number }> => {
  const authUser: AuthUser | null = await getAuthUser();
  if (!authUser) return { error: "your session expired. please log in", code: 401 };

  if (newRelative.userId != parseInt(authUser.id as string) && authUser.role != "ADMIN")
    return { error: "You don't have permission to update to someone's relatives", code: 401 };

  const { relative, db_error } = await dbUpdateRelative(newRelative);
  if (!relative || db_error) return { error: "Failed to update relative info", code: 500 };
  return { success: "Updated relative info.", relative, code: 201 };
};

/**
 * Returns records with relationship under user by userId. Validates if user authorized
 * @param userId number
 */
export const getRecordsWithRelativesInfoUnderUserId = async (
  userId: number | undefined
): Promise<{ success?: string; recordsWithRelationship?: RecordWithRelationship[]; error?: string; db_error?: string; code: number }> => {
  if (typeof userId != "number") return { error: "user id is required and must be number", code: 403 };

  const result = await getHouseholdByUserId(userId); // Validates if user authorized
  if (!result.household) return result;

  const { records, db_error } = await dbGetRecordsUnderHouseholdId(result.household.id);
  if (db_error) return { error: `Failed to get records for user by id '${userId}'.`, db_error, code: 500 };
  if (!records || !records.length) return { error: `No records found for user by id '${userId}'.`, code: 404 };

  const { relatives } = await getRelativesUnderUserId(userId);

  const recordsWithRelationship: RecordWithRelationship[] = [];

  if (relatives?.length) {
    records.forEach((record) => {
      const relative = relatives.find((r) => r.recordId == record.id) || undefined;
      recordsWithRelationship.push({ relative, record });
    });
  } else {
    records.forEach((record) => {
      recordsWithRelationship.push({ relative: undefined, record });
    });
  }

  return { success: `Records with relationship found for user by id '${userId}'`, recordsWithRelationship, code: 200 };
};

/**
 * Returns records with relationship under user by email. Validates if user authorized
 * @param userId number
 */
export const getRecordsWithRelativesInfoUnderUserEmail = async (
  email: string | undefined
): Promise<{ success?: string; recordsWithRelationship?: RecordWithRelationship[]; error?: string; db_error?: string; code: number }> => {
  if (typeof email != "string" || !email.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/))
    return { error: "user email is required and must be email format", code: 403 };

  const result = await getHouseholdByUserEmail(email); // Validates if user authorized
  if (!result.household) return result;

  const { records, db_error } = await dbGetRecordsUnderHouseholdId(result.household.id);
  if (db_error) return { error: `Failed to get records for user with email '${email}'.`, db_error, code: 500 };
  if (!records || !records.length) return { error: `No records found for user with email '${email}'.`, code: 404 };

  const { relatives } = await getRelativesUnderUserEmail(email);

  const recordsWithRelationship: RecordWithRelationship[] = [];

  if (relatives?.length) {
    records.forEach((record) => {
      const relative = relatives.find((r) => r.recordId == record.id) || undefined;
      recordsWithRelationship.push({ relative, record });
    });
  } else {
    records.forEach((record) => {
      recordsWithRelationship.push({ relative: undefined, record });
    });
  }

  return { success: `Records with relationship found for user with email '${email}'`, recordsWithRelationship, code: 200 };
};

/**
 * Saves record with relationship info under household by userId provided in arguments object. Validates if user authorized
 * @param values is object of { userId: number,
 *   relationship: "SELF" | "SPOUSE" | "PARTNER" | "BIOLOGICAL_CHILD" | "ADOPTED_CHILD" | \
 *                 "STEP_CHILD" | "COSINE" | "PARENT" | "GRANDCHILD" | "GRANDPARENT" | \
 *                 "OTHER_RELATIVE" | "OTHER_NON_RELATIVE" | "ROOMMATE_HOUSEMATE";
 *   firstName: string, lastName: string, dob: "MM/dd/yyyy", gender: "MALE" | "FEMALE", telephone: number | undefined,
 *   hispanic: "NO" | "MEXICAN" | "PUERTO_RICAN" | "CUBAN" | "OTHER" | "NO_ANSWER",
 *   hispanicOther: string | undefined
 *   race: "WHITE" | "BLACK" | "CHINESE" | "FILIPINO" | "ASIAN_INDIAN" | \
 *         "VIETNAMESE" | "KOREAN" | "JAPANESE" | "OTHER_ASIAN" | "NATIVE_HAWAIIAN" | \
 *         "SAMOAN" | "CHAMORRO" | "OTHER_PACIFIC" | "OTHER" | "NO_ANSWER",
 *   raceOther: string | undefined,
 *   otherStay: "NO" | "COLLEGE" | "MILITARY_ASSIGNMENT" | "JOB_OR_BUSINESS" | "NURSING_HOME" | \
 *              "WITH_PARENT_OR_OTHER_RELATIVE" | "SEASONAL_OR_SECOND_RESIDENT" | \
 *              "JAIL_OR_PRISON" | "OTHER", }
 */
export const saveRecordWithRelationship = async (
  values: z.infer<typeof RecordWithRelationshipSchema>
): Promise<{ success?: string; recordWithRelationship?: RecordWithRelationship; error?: string; data?: any; db_error?: string; code: number }> => {
  const validatedFields = RecordWithRelationshipSchema.safeParse(values);
  console.log(JSON.stringify(validatedFields.error?.errors, null, 2));
  if (!validatedFields.success) return { error: "Invalid fields!", data: validatedFields.error?.errors, code: 403 };

  const { userId, relationship, firstName, lastName, dob, gender, telephone, hispanic, hispanicOther, race, raceOther, otherStay } =
    validatedFields.data;

  const result1 = await getHouseholdByUserId(userId);
  if (!result1.household) {
    if (result1.code == 401) return { error: "You don't have permission to save record under someone's household", code: 401 };
    return { error: "User does not have household created yet.", code: 403 };
  }

  const result2 = await dbGetRelationshipsUnderUserId(userId);
  if (relationship == "SELF" && result2.relatives?.find((r) => r.relationship == "SELF"))
    return { error: "You already have SELF record.", code: 403 };

  const { record, db_error } = await dbSaveRecord({
    firstName,
    lastName,
    dob: new Date(dob),
    gender,
    telephone: telephone || null,
    householdId: result1.household.id,
    hispanic,
    hispanicOther: hispanicOther && hispanicOther.length > 0 ? hispanicOther : null,
    race,
    raceOther: raceOther && raceOther.length > 0 ? raceOther : null,
    otherStay,
  });
  console.log(record);
  console.log(db_error);
  if (!record || db_error) return { error: "Failed to save new record.", db_error, code: 500 };

  const result3 = await dbSaveRelative({
    userId,
    relationship,
    recordId: record.id,
  });
  if (!result3.relative || result3.db_error) return { error: "failed to assign relationship, but record is saved, please edit it.", code: 500 };

  const recordWithRelationship: RecordWithRelationship = { record, relative: result3.relative };
  return { success: `Successfully saved new record for '${relationship}'`, recordWithRelationship, code: 201 };
};

/**
 * Updates record with relationship info under household by userId and record id provided in arguments object. Validates if user authorized
 * @param values is object of { id: number, userId: number,
 *   relationship: "SELF" | "SPOUSE" | "PARTNER" | "BIOLOGICAL_CHILD" | "ADOPTED_CHILD" | \
 *                 "STEP_CHILD" | "COSINE" | "PARENT" | "GRANDCHILD" | "GRANDPARENT" | \
 *                 "OTHER_RELATIVE" | "OTHER_NON_RELATIVE" | "ROOMMATE_HOUSEMATE";
 *   firstName: string, lastName: string, dob: "MM/dd/yyyy", gender: "MALE" | "FEMALE", telephone: number | undefined,
 *   hispanic: "NO" | "MEXICAN" | "PUERTO_RICAN" | "CUBAN" | "OTHER" | "NO_ANSWER",
 *   hispanicOther: string | undefined
 *   race: "WHITE" | "BLACK" | "CHINESE" | "FILIPINO" | "ASIAN_INDIAN" | \
 *         "VIETNAMESE" | "KOREAN" | "JAPANESE" | "OTHER_ASIAN" | "NATIVE_HAWAIIAN" | \
 *         "SAMOAN" | "CHAMORRO" | "OTHER_PACIFIC" | "OTHER" | "NO_ANSWER",
 *   raceOther: string | undefined,
 *   otherStay: "NO" | "COLLEGE" | "MILITARY_ASSIGNMENT" | "JOB_OR_BUSINESS" | "NURSING_HOME" | \
 *              "WITH_PARENT_OR_OTHER_RELATIVE" | "SEASONAL_OR_SECOND_RESIDENT" | \
 *              "JAIL_OR_PRISON" | "OTHER", }
 */
export const updateRecordWithRelationship = async (
  values: z.infer<typeof UpdateRecordWithRelationshipSchema>
): Promise<{ success?: string; recordWithRelationship?: RecordWithRelationship; error?: string; data?: any; db_error?: string; code: number }> => {
  const validatedFields = UpdateRecordWithRelationshipSchema.safeParse(values);
  console.log(JSON.stringify(validatedFields.error?.errors, null, 2));
  if (!validatedFields.success) return { error: "Invalid fields!", data: validatedFields.error?.errors, code: 403 };

  const { id, userId, relationship, firstName, lastName, dob, gender, telephone, hispanic, hispanicOther, race, raceOther, otherStay } =
    validatedFields.data;

  const result1 = await getHouseholdByUserId(userId);
  if (!result1.household) {
    if (result1.code == 401) return { error: "You don't have permission to save record under someone's household", code: 401 };
    return { error: "User does not have household created yet.", code: 403 };
  }

  const result2 = await dbGetRelationshipsUnderUserId(userId);
  const relative = result2.relatives?.find((r) => r.recordId == id);
  if (relative?.relationship != "SELF" && relationship == "SELF" && result2.relatives?.find((r) => r.relationship == "SELF"))
    return { error: "You already have SELF record.", code: 403 };

  const { record, db_error } = await dbUpdateRecord({
    id,
    firstName,
    lastName,
    dob: new Date(dob),
    gender,
    telephone: telephone || null,
    householdId: result1.household.id,
    hispanic,
    hispanicOther: hispanicOther && hispanicOther.length > 0 ? hispanicOther : null,
    race,
    raceOther: raceOther && raceOther.length > 0 ? raceOther : null,
    otherStay,
  });
  if (!record || db_error) return { error: "Failed to update record.", db_error, code: 500 };

  let result3;

  if (relative) {
    relative.relationship = relationship;
    result3 = await dbUpdateRelative(relative);
  } else {
    result3 = await dbSaveRelative({
      userId,
      relationship,
      recordId: record.id,
    });
  }
  if (!result3.relative || result3.db_error) return { error: "failed to assign relationship, but record is updated, please edit it.", code: 500 };

  const recordWithRelationship: RecordWithRelationship = { record, relative: result3.relative };
  return { success: `Successfully updated record for '${relationship}'`, recordWithRelationship, code: 201 };
};
