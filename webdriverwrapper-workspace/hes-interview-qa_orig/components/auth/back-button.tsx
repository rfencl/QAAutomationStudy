"use client";

import Link from "next/link";
import { Button } from "../ui/button";

interface BackButtonprops {
  href: string;
  label: string;
}

export const BackButton = ({ href, label }: BackButtonprops) => {
  return (
    <Button variant="link" className="font-normal" size="sm" asChild>
      <Link className="back-button btn" href={href}>
        {label}
      </Link>
    </Button>
  );
};
