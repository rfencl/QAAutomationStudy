import { Lienholder } from "@prisma/client";
import { Checkbox } from "@/components/ui/checkbox";
import LienholderCombobox from "./lienholder-combobox";
import { ChangeEvent, Dispatch, SetStateAction, useEffect, useState } from "react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { getAllLienholders, saveLienholder } from "@/actions/actionsHousehold";
import { CheckedState } from "@radix-ui/react-checkbox";

interface LienholderFormProps {
  lienholderId: number | null;
  newLienholderId: number | null;
  setNewLienholderId: Dispatch<SetStateAction<number | null>>;
}

export default function LienholderForm({ lienholderId, newLienholderId, setNewLienholderId }: LienholderFormProps) {
  const [checked, setChecked] = useState<boolean>(!!lienholderId);
  const [addEnabled, setAddEnabled] = useState(false);
  const [lienholder, setLienholder] = useState<Lienholder | undefined>(undefined);
  const [name, setName] = useState<string>("");
  const [lienholders, setLienholders] = useState<Lienholder[]>([]);

  const onCheck = (checkedState: CheckedState) => {
    console.log(checkedState);
    if (checkedState) setChecked(true);
    else {
      console.log("cancelling adding lienholder");
      cancelEdit();
      setNewLienholderId(() => null);
      setChecked(false);
    }
  };

  const cancelEdit = () => {
    setName(lienholder?.name || "");
    setAddEnabled(false);
  };

  const nameChange = (e: ChangeEvent<HTMLInputElement>) => {
    e.preventDefault();
    setName(e.target.value);
  };

  const saveLienholderHandler = () => {
    saveLienholder(name).then((data) => {
      fetchLienholders().finally(() => {
        if (data.lienholder) {
          setLienholder(data.lienholder);
          setNewLienholderId(data.lienholder.id);
        }
      });
    });
  };

  const fetchLienholders = async () => {
    getAllLienholders().then((data) => {
      if (data.lienholders) setLienholders(data.lienholders);
    });
  };

  useEffect(() => {
    fetchLienholders();
  }, []);

  return (
    <div className="col-span-12 items-center grid grid-cols-12 gap-2">
      <div className="lienholder-actions flex justify-between items-center col-span-12">
        <div className="checkbox-container flex space-x-2">
          <Checkbox
            id="have-lienholder"
            checked={checked}
            onCheckedChange={(e) => {
              onCheck(e);
            }}
          />
          <label htmlFor="have-lienholder" className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
            Lienholder?
          </label>
        </div>
        {addEnabled ? (
          <>
            <Button
              variant="link"
              disabled={!checked}
              type="button"
              onClick={() => {
                saveLienholderHandler();
              }}
            >
              {lienholder ? "Update" : "Save"}
            </Button>
            <Button
              variant="link"
              disabled={!checked}
              type="button"
              onClick={() => {
                cancelEdit();
              }}
            >
              Cancel
            </Button>
          </>
        ) : (
          <Button
            variant="link"
            disabled={!checked}
            type="button"
            onClick={() => {
              setAddEnabled(true);
            }}
          >
            {lienholder ? "Update" : "Add"}
          </Button>
        )}
      </div>
      {checked ? (
        addEnabled ? (
          <Input
            id="lienholder-name-input"
            className="lienholder-name-input col-span-12"
            placeholder="Please type your lienholder name here."
            value={name}
            onChange={(e) => {
              nameChange(e);
            }}
          ></Input>
        ) : (
          <LienholderCombobox
            lienholders={lienholders}
            lienholderId={lienholderId}
            newLienholderId={newLienholderId}
            setNewLienholderId={setNewLienholderId}
          />
        )
      ) : (
        <div className="inline-flex items-center justify-start whitespace-nowrap border border-input bg-background rounded-md col-span-12 text-sm font-medium h-9 px-4">
          <p>No lienholder</p>
        </div>
      )}
    </div>
  );
}
