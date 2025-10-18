"use client";

import { Dispatch, SetStateAction } from "react";
import UpdateRecordDialog from "./update-record-dialog";
import { AuthUser, RecordWithRelationship } from "@/types/types";
import { DeleteRecordAlertDialog } from "./delete-record-alert";

interface RecordCardProps {
  authUser: AuthUser;
  recordWithRelationship: RecordWithRelationship;
  setEditRecordDialogOpen: Dispatch<SetStateAction<boolean>>;
}

export default function RecordCard({ authUser, recordWithRelationship, setEditRecordDialogOpen }: RecordCardProps) {
  return (
    <div className="record-card p-4 flex flex-wrap gap-2 items-start justify-start sm:col-span-6 border-b-2 rounded-md shadow hover:bg-slate-100">
      <div className="w-full inline-flex items-center justify-between">
        <p>
          <b>Relationship: </b>
          {recordWithRelationship.relative ? recordWithRelationship.relative.relationship : "not selected"}
        </p>
        <div className="actions">
          <UpdateRecordDialog authUser={authUser} recordWithRelationship={recordWithRelationship} setEditRecordDialogOpen={setEditRecordDialogOpen} />
          {recordWithRelationship && (
            <DeleteRecordAlertDialog recordWithRelationship={recordWithRelationship} setEditRecordDialogOpen={setEditRecordDialogOpen} />
          )}
        </div>
      </div>
      <p className="full-name">
        <b>Full name:</b> {recordWithRelationship.record.firstName} {recordWithRelationship.record.lastName}
      </p>
      <p className="dob">
        <b>Date of Birth:</b> {recordWithRelationship.record.dob.toLocaleDateString()}
      </p>
      <p className="gender">
        <b>Gender:</b> {recordWithRelationship.record.gender}
      </p>
      {recordWithRelationship.record.telephone && (
        <p id="telephone">
          <b>Telephone:</b> {recordWithRelationship.record.telephone}
        </p>
      )}
      <div className="inline-flex items-center justify-start gap-2">
        <p className="hispanic">
          <b>Hispanic:</b> {recordWithRelationship.record.hispanic}
        </p>
        {recordWithRelationship.record.hispanicOther && <p className="hispanic-other">{recordWithRelationship.record.hispanicOther}</p>}
      </div>
      <div className="inline-flex items-center justify-start gap-2">
        <p className="race">
          <b>Race:</b> {recordWithRelationship.record.race}
        </p>
        {recordWithRelationship.record.raceOther && <p className="race-other">{recordWithRelationship.record.raceOther}</p>}
      </div>
      <p className="other-stay">
        <b>Other stay:</b> {recordWithRelationship.record.otherStay}
      </p>
    </div>
  );
}
