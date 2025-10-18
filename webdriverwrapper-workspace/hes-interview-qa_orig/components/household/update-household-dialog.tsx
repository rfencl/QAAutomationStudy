"use client";

import React, { useState, useTransition } from "react";
import { Dispatch, SetStateAction } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { HOME_TYPE, HouseholdSchema, OWNERSHIP, STATES } from "@/schemas";
import { z } from "zod";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { FormSuccess } from "@/components/form-success";
import { FormError } from "@/components/form-error";
import { Button } from "@/components/ui/button";
import { Household } from "@prisma/client";
import { Select } from "@radix-ui/react-select";
import { SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { saveHousehold, updateHousehold } from "@/actions/actionsHousehold";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import LienholderForm from "@/components/household/lienholder-form";
import { AuthUser } from "@/types/types";

interface UpdateAddressDialogProps {
  // isLoading: boolean;
  setEditHouseholdOpen: Dispatch<SetStateAction<boolean>>;
  household: Household | undefined;
  authUser: AuthUser;
}

export default function UpdateHouseholdDialog({ authUser, household, setEditHouseholdOpen }: UpdateAddressDialogProps) {
  const [error, setError] = useState<string | undefined>(undefined);
  const [success, setSuccess] = useState<string | undefined>(undefined);
  const [isPending, startTransition] = useTransition();
  const lienholderId: number | null = household?.lienholderId || null;
  // newLienholderId will be in applied only after form submission
  // otherwise old lienholderId will be displayed in case user cancels
  const [newLienholderId, setNewLienholderId] = useState<number | null>(lienholderId);

  const form = useForm<z.infer<typeof HouseholdSchema>>({
    resolver: zodResolver(HouseholdSchema),
    defaultValues: {
      userId: parseInt(authUser.id as string),
      homeType: household?.homeType,
      ownership: household?.ownership,
      lienholderId: lienholderId || null,
      address1: household?.address1 || "",
      address2: household?.address2 || "",
      city: household?.city || "",
      state: household?.state,
      zip: household?.zip || "",
    },
  });

  const onSubmit = (values: z.infer<typeof HouseholdSchema>) => {
    setError(undefined);
    setSuccess(undefined);
    startTransition(() => {
      values.lienholderId = newLienholderId || null;
      console.log(values);
      if (!household)
        saveHousehold(values).then((data) => {
          if (data.error) setError(data.error);
          else {
            setSuccess(data.success);
          }
        });
      else
        updateHousehold({ id: household.id, ...values }).then((data) => {
          if (data.error) setError(data.error);
          else {
            setSuccess(data.success);
          }
        });
    });
  };
  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button
          variant="link"
          name="edit-household-button btn"
          type="button"
          onClick={() => {
            setEditHouseholdOpen(true);
          }}
        >
          {household ? "Edit household" : "Add household"}
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Update your household</DialogTitle>
        </DialogHeader>
        <DialogDescription>Your household information will be attached to every member in your household.</DialogDescription>
        <Form {...form}>
          <form name="register-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            <div className="grid grid-cols-12 gap-2">
              <FormField
                control={form.control}
                name="homeType"
                render={({ field }) => (
                  <FormItem id="homeType" className="col-span-6">
                    <FormLabel>Home type</FormLabel>
                    <Select {...field} disabled={isPending} onValueChange={field.onChange}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue defaultValue={household?.homeType || "jhbjhb"} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {HOME_TYPE.map((type) => (
                          <SelectItem key={type} value={type}>
                            {type}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              ></FormField>
              <FormField
                control={form.control}
                name="ownership"
                render={({ field }) => (
                  <FormItem id="ownership" className="col-span-6">
                    <FormLabel>Ownership</FormLabel>
                    <Select {...field} disabled={isPending} onValueChange={field.onChange}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue defaultValue={household?.ownership} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {OWNERSHIP.map((option) => (
                          <SelectItem key={option} value={option}>
                            {option}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              ></FormField>
              <LienholderForm lienholderId={lienholderId} newLienholderId={newLienholderId} setNewLienholderId={setNewLienholderId} />
              <FormField
                control={form.control}
                name="address1"
                render={({ field }) => (
                  <FormItem className="col-span-12">
                    <FormLabel>Address line 1</FormLabel>
                    <FormControl>
                      <Input id="address1" {...field} disabled={isPending} placeholder="Street address"></Input>
                    </FormControl>
                    <FormMessage></FormMessage>
                  </FormItem>
                )}
              ></FormField>
              <FormField
                control={form.control}
                name="address2"
                render={({ field }) => (
                  <FormItem className="col-span-12">
                    <FormLabel>Address line 2</FormLabel>
                    <FormControl>
                      <Input id="address2" {...field} disabled={isPending} placeholder="Apartment, Suite, Floor"></Input>
                    </FormControl>
                    <FormMessage></FormMessage>
                  </FormItem>
                )}
              ></FormField>
              <FormField
                control={form.control}
                name="city"
                render={({ field }) => (
                  <FormItem className="col-span-6">
                    <FormLabel>City</FormLabel>
                    <FormControl>
                      <Input id="city" {...field} disabled={isPending} placeholder="City, Town, Borrow"></Input>
                    </FormControl>
                    <FormMessage></FormMessage>
                  </FormItem>
                )}
              ></FormField>
              <FormField
                control={form.control}
                name="state"
                render={({ field }) => (
                  <FormItem id="state" className="col-span-3">
                    <FormLabel>State</FormLabel>
                    <Select {...field} disabled={isPending} onValueChange={field.onChange}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {STATES.map((state) => (
                          <SelectItem key={state} value={state}>
                            {state}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              ></FormField>
              <FormField
                control={form.control}
                name="zip"
                render={({ field }) => (
                  <FormItem className="col-span-3">
                    <FormLabel>Zip code</FormLabel>
                    <FormControl>
                      <Input id="zip" {...field} disabled={isPending}></Input>
                    </FormControl>
                    <FormMessage></FormMessage>
                  </FormItem>
                )}
              ></FormField>
            </div>
            <FormError message={error} />
            <FormSuccess message={success} />
            <DialogFooter>
              <div className="w-full flex justify-between col-span-12">
                <DialogClose className="w-full flex mr-5">
                  <Button
                    variant="outline"
                    name="cancel-password-update-button btn"
                    type="reset"
                    className="w-full"
                    disabled={isPending}
                    onClick={() => {
                      form.reset();
                      setEditHouseholdOpen(false);
                    }}
                  >
                    {success ? "OK" : "Cancel"}
                  </Button>
                </DialogClose>
                <Button variant="secondary" name="password-update-button btn" type="submit" className="w-full flex ml-5" disabled={isPending}>
                  Update
                </Button>
              </div>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
