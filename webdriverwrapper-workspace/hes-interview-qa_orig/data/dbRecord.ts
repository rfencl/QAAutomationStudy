import { db } from "@/lib/db";
import { Record, Relative } from "@prisma/client";

export const dbGetAllRecords = async (): Promise<{ records?: Record[]; db_error?: string }> => {
  try {
    const records: Record[] = await db.record.findMany();
    return { records };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbGetRecordById = async (id: number): Promise<{ record?: Record | null; db_error?: string }> => {
  try {
    const record: Record | null = await db.record.findUnique({
      where: { id },
    });
    return { record };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbGetRecordsUnderHouseholdId = async (householdId: number): Promise<{ records?: Record[]; db_error?: string }> => {
  try {
    const records: Record[] = await db.record.findMany({
      where: { householdId },
    });
    return { records };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbSaveRecord = async (newRecord: Omit<Record, "id">): Promise<{ record?: Record; db_error?: string }> => {
  try {
    const record: Record = await db.record.create({
      data: { ...newRecord },
    });
    return { record };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbUpdateRecord = async (newRecord: Record): Promise<{ record?: Record; db_error?: string }> => {
  try {
    const record: Record = await db.record.update({
      where: { id: newRecord.id },
      data: { ...newRecord },
    });
    return { record };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbDeleteRecordById = async (id: number): Promise<{ record?: Record; db_error?: string }> => {
  try {
    const record: Record = await db.record.delete({
      where: { id },
    });
    return { record };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbDeleteRecordsUnderHouseholdId = async (householdId: number): Promise<{ db_error?: string }> => {
  try {
    const record = await db.record.deleteMany({
      where: { householdId },
    });
    console.log(record);
    return {};
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbGetRelationshipsUnderUserId = async (userId: number): Promise<{ relatives?: Relative[]; db_error?: string }> => {
  try {
    const relatives: Relative[] = await db.relative.findMany({
      where: { userId },
    });
    return { relatives };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbGetRelationshipsUnderUserEmail = async (email: string): Promise<{ relatives?: Relative[]; db_error?: string }> => {
  try {
    const relatives: Relative[] = await db.relative.findMany({
      where: { User: { email } },
    });
    return { relatives };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbSaveRelative = async (newRelative: Omit<Relative, "id">): Promise<{ relative?: Relative; db_error?: string }> => {
  try {
    const relative: Relative = await db.relative.create({ data: newRelative });
    return { relative };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbUpdateRelative = async (newRelative: Relative): Promise<{ relative?: Relative; db_error?: string }> => {
  try {
    const relative: Relative = await db.relative.update({
      where: { id: newRelative.id },
      data: newRelative,
    });
    return { relative };
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbDeleteRelativeById = async (id: number): Promise<{ db_error?: string }> => {
  try {
    const relative = await db.relative.delete({ where: { id } });
    return {};
  } catch (e: any) {
    return { db_error: e.message };
  }
};

export const dbDeleteAllRelativeInfoUnderUserId = async (userId: number): Promise<{ db_error?: string }> => {
  try {
    const relative = await db.relative.deleteMany({ where: { userId } });
    return {};
  } catch (e: any) {
    return { db_error: e.message };
  }
};
