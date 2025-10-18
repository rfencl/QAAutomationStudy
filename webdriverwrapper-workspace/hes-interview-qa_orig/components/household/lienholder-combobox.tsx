"use client";

import { CaretSortIcon, CheckIcon } from "@radix-ui/react-icons";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Dispatch, SetStateAction, useEffect, useState } from "react";
import { Lienholder } from "@prisma/client";

interface LienholderComboboxProps {
  lienholders: Lienholder[];
  lienholderId: number | null;
  newLienholderId: number | null;
  setNewLienholderId: Dispatch<SetStateAction<number | null>>;
}

export default function LienholderCombobox({ lienholders, lienholderId, newLienholderId, setNewLienholderId }: LienholderComboboxProps) {
  const [open, setOpen] = useState(false);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline" role="combobox" aria-expanded={open} className="col-span-12 justify-between">
          {newLienholderId
            ? lienholders.find((l) => l.id === newLienholderId)?.name
            : lienholderId
            ? lienholders.find((l) => l.id === lienholderId)?.name
            : "Select lienholder"}
          <CaretSortIcon className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="col-span-12 p-0">
        <Command>
          <CommandInput placeholder="Search framework..." className="h-9" />
          <CommandList>
            <CommandEmpty>No lienholders found</CommandEmpty>
            <CommandGroup>
              {lienholders.map((l) => (
                <CommandItem
                  key={l.id}
                  value={String(l.id)}
                  onSelect={(currentValue) => {
                    setNewLienholderId(currentValue === String(lienholderId) ? null : parseInt(currentValue));
                    setOpen(false);
                  }}
                >
                  {l.name}
                  <CheckIcon className={cn("ml-auto h-4 w-4", lienholderId === l.id ? "opacity-100" : "opacity-0")} />
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
