import * as z from "zod";

export const LoginSchema = z.object({
  email: z.string({ required_error: "email is required." }).email({
    message: "has to be proper email format.",
  }),
  password: z
    .string({ required_error: "password is required." })
    .min(1, {
      message: "minimum 1 character is required.",
    })
    .max(20, {
      message: "Maximum 20 characters.",
    }),
});

export const ROLE = ["USER", "ADMIN"] as const;

export const RegisterUserSchema = z.object({
  email: z.string({ required_error: "email is required." }).email({
    message: "has to be proper email format.",
  }),
  password: z
    .string({ required_error: "password is required." })
    .min(6, {
      message: "minimum 6 character is required.",
    })
    .max(20, {
      message: "Maximum 20 characters.",
    }),
  name: z.string({ required_error: "Name is required." }).min(1, {
    message: "minimum 1 character is required.",
  }),
  image: z.string(),
});

export const UpdateUserSchema = z.object({
  id: z.number({ required_error: "id is required." }).min(1, {
    message: "minimum 1 digit required.",
  }),
  email: z.string({ required_error: "email is required." }).email({
    message: "has to be proper email format.",
  }),
  name: z
    .string()
    .min(1, {
      message: "minimum 1 character is required.",
    })
    .optional(),
  image: z.string().optional(),
  role: z.enum(ROLE).optional(),
  householdId: z.number().optional(),
});

export const UpdatePasswordSchema = z.object({
  id: z.number({ required_error: "id is required." }).min(1, {
    message: "minimum 1 digit required.",
  }),
  oldPassword: z
    .string({ required_error: "old password is required." })
    .min(6, {
      message: "Minimum 6 character required.",
    })
    .max(20, {
      message: "Maximum 20 characters.",
    })
    .optional(),
  newPassword: z
    .string({ required_error: "new password is required." })
    .min(6, {
      message: "Minimum 6 character required.",
    })
    .max(20, {
      message: "Maximum 20 characters.",
    }),
});

export const STATES = [
  "AL",
  "AK",
  "AZ",
  "AR",
  "CA",
  "CO",
  "CT",
  "DE",
  "DC",
  "FL",
  "GA",
  "HI",
  "ID",
  "IL",
  "IN",
  "IA",
  "KS",
  "KY",
  "LA",
  "ME",
  "MD",
  "MA",
  "MI",
  "MN",
  "MS",
  "MO",
  "MT",
  "NE",
  "NV",
  "NH",
  "NJ",
  "NM",
  "NY",
  "NC",
  "ND",
  "OH",
  "OK",
  "OR",
  "PA",
  "RI",
  "SC",
  "SD",
  "TN",
  "TX",
  "UT",
  "VT",
  "VA",
  "WA",
  "WV",
  "WI",
  "WY",
] as const;

export const HOME_TYPE = ["HOUSE", "APARTMENT", "MOBILE_HOME", "SHELTER"] as const;

export const OWNERSHIP = ["MORTGAGE", "OWN", "RENT", "FREE_LIVING"] as const;

export const HouseholdSchema = z.object({
  userId: z.number({ required_error: "id is required." }).min(1, {
    message: "minimum 1 digit required.",
  }),
  homeType: z.enum(HOME_TYPE),
  ownership: z.enum(OWNERSHIP),
  lienholderId: z.coerce.number().nullable(),
  address1: z.string({ required_error: "Address 1 is required." }),
  address2: z.string().optional(),
  city: z.string({ required_error: "City is required." }),
  state: z.enum(STATES),
  zip: z.string({ required_error: "zip code is required." }).regex(/^\d{5}$/, { message: "zip code must be 5 digits." }),
});

export const UpdateHouseholdSchema = z.object({
  id: z.number({ required_error: "id is required." }).min(1, {
    message: "minimum 1 digit required.",
  }),
  homeType: z.enum(HOME_TYPE),
  ownership: z.enum(OWNERSHIP),
  lienholderId: z.coerce.number().nullable(),
  address1: z.string({ required_error: "Address 1 is required." }),
  address2: z.string().optional(),
  city: z.string({ required_error: "City is required." }),
  state: z.enum(STATES),
  zip: z.string({ required_error: "zip code is required." }).regex(/^\d{5}$/, { message: "zip code must be 5 digits." }),
});

export const GENDER = ["MALE", "FEMALE"] as const;

export const HISPANIC = ["NO", "MEXICAN", "PUERTO_RICAN", "CUBAN", "OTHER", "NO_ANSWER"] as const;

export const RACE = [
  "WHITE",
  "BLACK",
  "CHINESE",
  "FILIPINO",
  "ASIAN_INDIAN",
  "VIETNAMESE",
  "KOREAN",
  "JAPANESE",
  "OTHER_ASIAN",
  "NATIVE_HAWAIIAN",
  "SAMOAN",
  "CHAMORRO",
  "OTHER_PACIFIC",
  "OTHER",
  "NO_ANSWER",
] as const;

export const OTHER_STAY = [
  "NO",
  "COLLEGE",
  "MILITARY_ASSIGNMENT",
  "JOB_OR_BUSINESS",
  "NURSING_HOME",
  "WITH_PARENT_OR_OTHER_RELATIVE",
  "SEASONAL_OR_SECOND_RESIDENT",
  "JAIL_OR_PRISON",
  "OTHER",
] as const;

export const RecordSchema = z.object({
  firstName: z.string({ required_error: "First Name is required." }).regex(/^[a-zA-Z0-9']*$/, { message: "Only alphabets and digits are allowed." }),
  lastName: z.string({ required_error: "Last Name is required." }).regex(/^[a-zA-Z0-9']*$/, { message: "Only alphabets and digits are allowed." }),
  // dob: z.date({ invalid_type_error: "Date has to be in YYYY-MM-DD format.", required_error: "Date of Birth is required." }),
  // .string({ required_error: "Date of Birth is required." })
  dob: z.string().regex(/^(0?[1-9]|1[012])[\/\-](0?[1-9]|[12][0-9]|3[01])[\/\-]\d{4}$/, { message: "Date has to be in MM/dd/yyyy format." }),
  gender: z.enum(GENDER),
  telephone: z
    .string()
    .regex(/^(\d{5,10})?$/, "must be 5 to 10 digits or empty")
    .optional(),
  householdId: z.number({ required_error: "Household id is required." }),
  hispanic: z.enum(HISPANIC),
  hispanicOther: z.string().optional(),
  race: z.enum(RACE),
  raceOther: z.string().optional(),
  otherStay: z.enum(OTHER_STAY),
});

export const UpdateRecordSchema = z.object({
  id: z.number({ required_error: "id is required." }).min(1, {
    message: "minimum 1 digit required.",
  }),
  firstName: z.string({ required_error: "First Name is required." }).regex(/^[a-zA-Z0-9]$/, { message: "Only alphabets and digits are allowed." }),
  lastName: z.string({ required_error: "Last Name is required." }).regex(/^[a-zA-Z0-9]$/, { message: "Only alphabets and digits are allowed." }),
  // dob: z.date({ invalid_type_error: "Date has to be in YYYY-MM-DD format.", required_error: "Date of Birth is required." }),
  dob: z.string().regex(/^(0?[1-9]|1[012])[\/\-](0?[1-9]|[12][0-9]|3[01])[\/\-]\d{4}$/, { message: "Date has to be in MM/dd/yyyy format." }),
  gender: z.enum(GENDER),
  telephone: z
    .string()
    .regex(/^(\d{5,10})?$/, "must be 5 to 10 digits or empty")
    .optional(),
  householdId: z.number({ required_error: "Household id is required." }),
  hispanic: z.enum(HISPANIC),
  hispanicOther: z.string().optional(),
  race: z.enum(RACE),
  raceOther: z.string().optional(),
  otherStay: z.enum(OTHER_STAY),
});

export const RELATIONSHIP = [
  "SELF",
  "SPOUSE",
  "PARTNER",
  "BIOLOGICAL_CHILD",
  "ADOPTED_CHILD",
  "STEP_CHILD",
  "COSINE",
  "PARENT",
  "GRANDCHILD",
  "GRANDPARENT",
  "OTHER_RELATIVE",
  "OTHER_NON_RELATIVE",
  "ROOMMATE_HOUSEMATE",
] as const;

export const RecordWithRelationshipSchema = z.object({
  userId: z.number({ required_error: "user id is required." }),
  relationship: z.enum(RELATIONSHIP),
  firstName: z.string({ required_error: "First Name is required." }).regex(/^[a-zA-Z0-9']*$/, { message: "Only alphabets and digits are allowed." }),
  lastName: z.string({ required_error: "Last Name is required." }).regex(/^[a-zA-Z0-9']*$/, { message: "Only alphabets and digits are allowed." }),
  // dob: z.string().date(),
  // dob: z.date({ invalid_type_error: "Date has to be in mm/DD/yyyy or mm-DD-yyyy format. from zod.", required_error: "Date of Birth is required." }),
  // .string({ required_error: "Date of Birth is required." })
  dob: z.string().regex(/^(0?[1-9]|1[012])[\/\-](0?[1-9]|[12][0-9]|3[01])[\/\-]\d{4}$/, { message: "Date has to be in MM/dd/yyyy format." }),
  gender: z.enum(GENDER),
  telephone: z
    .string()
    .regex(/^(\d{5,10})?$/, "must be 5 to 10 digits or empty")
    .optional(),
  hispanic: z.enum(HISPANIC),
  hispanicOther: z.string().optional(),
  race: z.enum(RACE),
  raceOther: z.string().optional(),
  otherStay: z.enum(OTHER_STAY),
});

export const UpdateRecordWithRelationshipSchema = z.object({
  id: z.number({ required_error: "id is required." }).min(1, {
    message: "minimum 1 digit required.",
  }),
  userId: z.number({ required_error: "user id is required." }),
  relationship: z.enum(RELATIONSHIP),
  firstName: z.string({ required_error: "First Name is required." }).regex(/^[a-zA-Z0-9']*$/, { message: "Only alphabets and digits are allowed." }),
  lastName: z.string({ required_error: "Last Name is required." }).regex(/^[a-zA-Z0-9']*$/, { message: "Only alphabets and digits are allowed." }),
  // dob: z.string().date(),
  // dob: z.date({ invalid_type_error: "Date has to be in YYYY-MM-DD format.", required_error: "Date of Birth is required." }),
  // .string({ required_error: "Date of Birth is required." })
  dob: z.string().regex(/^(0?[1-9]|1[012])[\/\-](0?[1-9]|[12][0-9]|3[01])[\/\-]\d{4}$/, { message: "Date has to be in MM/dd/yyyy format." }),
  gender: z.enum(GENDER),
  telephone: z
    .string()
    .regex(/^(\d{5,10})?$/, "must be 5 to 10 digits or empty")
    .optional(),
  hispanic: z.enum(HISPANIC),
  hispanicOther: z.string().optional(),
  race: z.enum(RACE),
  raceOther: z.string().optional(),
  otherStay: z.enum(OTHER_STAY),
});
