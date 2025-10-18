// /

"use client";

import Image from "next/image";

export default function Home() {
  return (
    <>
      <header className="bg-white shadow">
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <h1 className="text-3xl font-bold tracking-tight text-gray-900" data-testid="header-title">
            About
          </h1>
        </div>
      </header>
      <div className="flex flex-1 flex-col justify-center px-6 py-12 lg:px-8">
        <main>
          <div className="sm:mx-auto sm:w-full sm:max-w-sm">
            <Image className="mx-auto h-60 w-60 rounded-full" src="/logo.jpg" alt="Your Company" height="3000" width="3000" data-testid="big-logo" />
            <h2 className="mt-10 text-center text-2xl font-bold leading-9 tracking-tight text-gray-900">Welcome to Census Registration</h2>
          </div>
          <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
            <p>This app is intended to be used by Quality Assurance Engineers for software testing training</p>
            <p>Author has no responsibility for any official or commercial usage</p>
            <p>You are using this app at your own risk</p>
            <p>Please DO NOT save any PII (Personal Identifiable information)</p>
            <p>All information has to be of unreal people</p>
            <p>This app has no any security protection for any information submitted</p>
          </div>
        </main>
      </div>
    </>
  );
}
