import { deleteRecordById } from "@/actions/actionsRecord";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Dispatch, SetStateAction, useState } from "react";
import { FormError } from "@/components/form-error";
import { FormSuccess } from "@/components/form-success";
import { RecordWithRelationship } from "@/types/types";

interface DeleteRecordAlertDialogProps {
  recordWithRelationship: RecordWithRelationship;
  setEditRecordDialogOpen: Dispatch<SetStateAction<boolean>>;
}

export function DeleteRecordAlertDialog({ recordWithRelationship, setEditRecordDialogOpen }: DeleteRecordAlertDialogProps) {
  const [error, setError] = useState<string | undefined>(undefined);
  const [success, setSuccess] = useState<string | undefined>(undefined);

  const recordDeleteHandler = () => {
    if (recordWithRelationship.relative?.relationship == "SELF") {
      setSuccess(undefined);
      setError("Deleting SELF record is prohibited. You can only edit SELF record.");
    } else
      deleteRecordById(recordWithRelationship.record.id).then((data) => {
        if (data.error) setError(data.error);
        else {
          setSuccess(data.success);
          setTimeout(() => {
            setEditRecordDialogOpen(false);
          }, 2000);
        }
        console.log(data);
      });
  };
  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button
          variant="link"
          name="delete-record-button btn"
          type="button"
          size="tight"
          onClick={() => {
            setEditRecordDialogOpen(true);
          }}
        >
          Delete
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
          <AlertDialogDescription>
            This action cannot be undone. This will permanently delete record for {recordWithRelationship.record.firstName}
            {recordWithRelationship.record.lastName} data from our servers.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <FormError message={error} />
        <FormSuccess message={success} />
        <AlertDialogFooter>
          <AlertDialogCancel>{success ? "OK" : "Cancel"}</AlertDialogCancel>
          <Button variant="destructive" disabled={success != undefined} onClick={recordDeleteHandler}>
            Delete
          </Button>
          {/* <AlertDialogAction disabled={success != undefined} onClick={recordDeleteHandler}>
            Delete
          </AlertDialogAction> */}
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
