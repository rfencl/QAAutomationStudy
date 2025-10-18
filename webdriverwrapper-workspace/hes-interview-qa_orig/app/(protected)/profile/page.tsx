// /profile

"use client";

import { getAuthUser } from "@/actions/actionsAuth";
// import { auth } from "@/auth";
import { ProfileForm } from "@/components/auth/profile-form";
import { AuthUser } from "@/types/types";
import { useEffect, useState } from "react";

export default function ProfilePage() {
  const [authUser, setAuthUser] = useState<AuthUser | null>(null);

  useEffect(() => {
    getAuthUser().then((data) => {
      setAuthUser(data);
    });
  }, []);
  return (
    <>
      <header className="bg-white shadow">
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <h1 className="text-3xl font-bold tracking-tight text-gray-900">Profile</h1>
        </div>
      </header>
      <main>
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">{authUser ? <ProfileForm authUser={authUser} /> : <p>Loading . . .</p>}</div>
      </main>
    </>
  );
}
