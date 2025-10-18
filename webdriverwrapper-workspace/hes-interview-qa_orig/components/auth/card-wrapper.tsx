"use client";

import { Card, CardContent, CardHeader, CardFooter } from "@/components/ui/card";
import { Header } from "./header";
import { BackButton } from "./back-button";

interface CardWrapperProps {
  children: React.ReactNode;
  headerLabel?: string;
  backButtonLabel?: string;
  backButtonHref?: string;
}

export const CardWrapper = ({ children, headerLabel, backButtonLabel, backButtonHref }: CardWrapperProps) => {
  return (
    <Card className="card-wrapper mt-10 sm:mx-auto sm:w-full sm:max-w-sm">
      <CardHeader>{headerLabel ? <Header label={headerLabel}></Header> : null}</CardHeader>
      <CardContent>{children}</CardContent>
      {backButtonLabel && backButtonHref ? (
        <CardFooter className="w-full flex justify-center items-center">
          <BackButton href={backButtonHref} label={backButtonLabel}></BackButton>
        </CardFooter>
      ) : null}
    </Card>
  );
};
