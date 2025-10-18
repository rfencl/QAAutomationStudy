// /dashboards

"use client";

import { getAuthUser } from "@/actions/actionsAuth";
import HouseholdCard from "@/components/household/household-card";
import LoadingCard from "@/components/household/loading-card";
import { AuthUser } from "@/types/types";
import { useSession } from "next-auth/react";
import { useEffect, useState } from "react";

export default function LandingPage() {
  const [authUser, setAuthUser] = useState<AuthUser | null>(null);

  useEffect(() => {
    document.title = "Census App | Dashboard";
    getAuthUser().then((data) => {
      setAuthUser(data);
    });
  }, []);

  return (
    <div className="dashboard-container w-full ">
      {authUser ? <HouseholdCard authUser={authUser} /> : <LoadingCard className="loading-address-card" />}
    </div>
  );
}
