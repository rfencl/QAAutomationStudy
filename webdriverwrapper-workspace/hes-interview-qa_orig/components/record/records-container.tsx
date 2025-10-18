import { useEffect, useState } from "react";
import LoadingCard from "../household/loading-card";
import RecordCard from "./record-card";
import UpdateRecordDialog from "./update-record-dialog";
import { getRecordsWithRelativesInfoUnderUserId } from "@/actions/actionsRecord";
import { AuthUser, RecordWithRelationship } from "@/types/types";

interface RecordsContainerProps {
  authUser: AuthUser;
}

export default function RecordsContainer({ authUser }: RecordsContainerProps) {
  const [loading, setLoading] = useState<boolean>(true);
  const [recordsWithRelationship, setRecordsWithRelationship] = useState<RecordWithRelationship[]>([]);
  const [editRecordDialogOpen, setEditRecordDialogOpen] = useState<boolean>(false);

  useEffect(() => {
    if (!editRecordDialogOpen) {
      setLoading(true);
      getRecordsWithRelativesInfoUnderUserId(parseInt(authUser.id as string))
        .then((data) => {
          if (data.recordsWithRelationship) {
            setRecordsWithRelationship(data.recordsWithRelationship);
          }
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, [editRecordDialogOpen]);

  return (
    <div className={"records-container w-full mt-4 grid sm:grid-cols-12 gap-2"}>
      {loading ? (
        <LoadingCard className="loading-record-card sm:col-span-6 p-4 border-b-2 rounded-md shadow hover:bg-slate-100" />
      ) : recordsWithRelationship.length ? (
        <>
          {recordsWithRelationship.map((each, i) => (
            <RecordCard key={i} authUser={authUser} recordWithRelationship={each} setEditRecordDialogOpen={setEditRecordDialogOpen} />
          ))}
          <div className="add-record-card sm:col-span-6 inline-flex items-start justify-between p-2 border-b-2 rounded-md shadow hover:bg-slate-100">
            <p>Do you have anybody else?</p>
            <UpdateRecordDialog authUser={authUser} recordWithRelationship={undefined} setEditRecordDialogOpen={setEditRecordDialogOpen} />
          </div>
        </>
      ) : (
        <div className="no-records-card sm:col-span-6 inline-flex items-center justify-between p-2 border-b-2 rounded-md shadow hover:bg-slate-100">
          <p>You do not have any records yet.</p>
          <UpdateRecordDialog authUser={authUser} recordWithRelationship={undefined} setEditRecordDialogOpen={setEditRecordDialogOpen} />
        </div>
      )}
    </div>
  );
}
