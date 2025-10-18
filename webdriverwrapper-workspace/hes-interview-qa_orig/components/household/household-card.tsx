"use client";

import { getHouseholdByUserId, getLienholderById } from "@/actions/actionsHousehold";
import { Household, Lienholder } from "@prisma/client";
import { useEffect, useState } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import UpdateHouseholdDialog from "@/components/household/update-household-dialog";
import RecordsContainer from "@/components/record/records-container";
import { AuthUser } from "@/types/types";

interface AddressCardProps {
  authUser: AuthUser;
}

export default function HouseholdCard({ authUser }: AddressCardProps) {
  const [isLoading, setLoading] = useState(true);
  const [household, setHousehold] = useState<Household | undefined>(undefined);
  const [lienholder, setLienholder] = useState<Lienholder | undefined>(undefined);
  const [isEditHouseholdOpen, setEditHouseholdOpen] = useState(false);

  const getHousehold = () => {
    setLoading(true);
    getHouseholdByUserId(parseInt(authUser.id as string)).then((data) => {
      setHousehold(data.household as Household);
      if (data.household?.lienholderId) {
        getLienholderById(data.household.lienholderId)
          .then((data) => {
            setLienholder(data.lienholder);
          })
          .finally(() => {
            setLoading(false);
          });
      } else {
        setLienholder(undefined);
        setLoading(false);
      }
    });
  };

  useEffect(() => {
    console.log(`editHouseholdOpen = ${isEditHouseholdOpen}`);
    if (!isEditHouseholdOpen) getHousehold();
  }, [isEditHouseholdOpen]);

  return (
    <>
      <div className="address-card p-4 flex flex-col border-b-2 rounded-md shadow hover:bg-slate-100">
        <div className="flex justify-between items-center w-full">
          <p>Household information:</p>
          {!isLoading ? <UpdateHouseholdDialog authUser={authUser} household={household} setEditHouseholdOpen={setEditHouseholdOpen} /> : null}
        </div>
        {isLoading ? (
          <div className="space-y-2">
            <Skeleton className="h-4 w-80" />
            <Skeleton className="h-4 w-80" />
            <Skeleton className="h-4 w-52" />
            <Skeleton className="h-4 w-52" />
          </div>
        ) : household ? (
          <div className="grid md:grid-cols-12 pt-1">
            <div className="md:col-span-6">
              <div className="w-full flex flex-initial space-x-2">
                <p>
                  <b>Type:</b> {household.homeType}
                </p>
                <p>
                  <b>Ownership:</b> {household.ownership}
                </p>
              </div>
              {lienholder ? (
                <p>
                  <b>Lienholder:</b> {lienholder.name}
                </p>
              ) : null}
            </div>
            <div className="md:col-span-6">
              <p>{household.address1}</p>
              {household.address2 ? <p>{household.address2}</p> : null}
              <p>{`${household.city}, ${household.state} ${household.zip}`}</p>
            </div>
          </div>
        ) : (
          <div className="flex flex-col pt-1">
            <p>You have not entered your household yet</p>
          </div>
        )}
      </div>
      {household && <RecordsContainer authUser={authUser} />}
    </>
  );
}
