import { Relative, UserRole } from "@prisma/client";
import { User } from "next-auth";

export interface Obj {
  [key: string]: any;
}

export interface AuthUser extends User {
  role: UserRole;
}

export interface RecordWithRelationship {
  relative: Relative | undefined;
  record: Record;
}
