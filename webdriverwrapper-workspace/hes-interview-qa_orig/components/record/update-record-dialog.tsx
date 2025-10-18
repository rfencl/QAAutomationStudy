"use client";

import React, { useState, useTransition } from "react";
import { Dispatch, SetStateAction } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { HISPANIC, OTHER_STAY, RACE, RecordWithRelationshipSchema, RELATIONSHIP } from "@/schemas";
import { z } from "zod";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { FormSuccess } from "@/components/form-success";
import { FormError } from "@/components/form-error";
import { Button } from "@/components/ui/button";
import { Select } from "@radix-ui/react-select";
import { SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
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
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { cn } from "@/lib/utils";
import { format } from "date-fns";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { saveRecordWithRelationship, updateRecordWithRelationship } from "@/actions/actionsRecord";
import { AuthUser, RecordWithRelationship } from "@/types/types";

interface UpdateRecordDialogProps {
  authUser: AuthUser;
  recordWithRelationship: RecordWithRelationship | undefined;
  setEditRecordDialogOpen: Dispatch<SetStateAction<boolean>>;
}

export default function UpdateRecordDialog({ authUser, recordWithRelationship, setEditRecordDialogOpen }: UpdateRecordDialogProps) {
  const [error, setError] = useState<string | undefined>(undefined);
  const [success, setSuccess] = useState<string | undefined>(undefined);
  const [hispanicOtherDisabled, setHispanicOtherDisabled] = useState<boolean>(recordWithRelationship?.record?.hispanic != "OTHER");
  const [raceOtherDisabled, setRaceOtherDisabled] = useState<boolean>(recordWithRelationship?.record?.race != "OTHER");

  const [isPending, startTransition] = useTransition();

  const form = useForm<z.infer<typeof RecordWithRelationshipSchema>>({
    resolver: zodResolver(RecordWithRelationshipSchema),
    defaultValues: {
      userId: parseInt(authUser.id as string),
      relationship: recordWithRelationship?.relative?.relationship,
      firstName: recordWithRelationship?.record?.firstName || "",
      lastName: recordWithRelationship?.record?.lastName || "",
      dob: recordWithRelationship?.record?.dob ? format(recordWithRelationship.record.dob, "MM/dd/yyyy") : undefined,
      gender: recordWithRelationship?.record?.gender || "",
      telephone: recordWithRelationship?.record?.telephone || undefined,
      hispanic: recordWithRelationship?.record?.hispanic || "",
      hispanicOther: recordWithRelationship?.record?.hispanicOther || "",
      race: recordWithRelationship?.record?.race || "",
      raceOther: recordWithRelationship?.record?.raceOther || "",
      otherStay: recordWithRelationship?.record?.otherStay || "",
    },
  });

  const onSubmit = (values: z.infer<typeof RecordWithRelationshipSchema>) => {
    setError(undefined);
    setSuccess(undefined);
    startTransition(() => {
      console.log(values);
      if (!recordWithRelationship)
        saveRecordWithRelationship(values).then((data) => {
          if (data.error) setError(data.error);
          else {
            setSuccess(data.success);
          }
        });
      else
        updateRecordWithRelationship({ id: recordWithRelationship.record.id, ...values }).then((data) => {
          console.log(data);
          if (data.error) setError(data.error);
          else {
            setSuccess(data.success);
          }
        });
    });
  };

  const hispanicHandler = (e: string) => {
    setHispanicOtherDisabled(e != "OTHER");
    if (e != "OTHER") form.setValue("hispanicOther", "");
  };

  const raceHandler = (e: string) => {
    setRaceOtherDisabled(e != "OTHER");
    if (e != "OTHER") form.setValue("raceOther", "");
  };

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button
          variant="link"
          name="edit-record-button btn"
          type="button"
          size="tight"
          onClick={() => {
            setEditRecordDialogOpen(true);
          }}
        >
          {recordWithRelationship ? "Edit" : "Add record"}
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{recordWithRelationship ? "Update existing person's data" : "Add new person to your household"}</DialogTitle>
          <DialogDescription>Any updates will be saved under your household.</DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form name="register-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid sm:grid-cols-12 gap-2">
              <FormField
                control={form.control}
                name="relationship"
                render={({ field }) => (
                  <FormItem id="relationship" className="sm:col-span-12">
                    <FormLabel>Relationship</FormLabel>
                    <Select
                      {...field}
                      disabled={isPending}
                      onValueChange={(e) => {
                        field.onChange(e);
                      }}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {RELATIONSHIP.map((r) => (
                          <SelectItem key={r} value={r}>
                            {r}
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
                name="firstName"
                render={({ field }) => (
                  <FormItem className="sm:col-span-6">
                    <FormLabel>First name</FormLabel>
                    <FormControl>
                      <Input id="firstName" {...field} disabled={isPending} placeholder="required"></Input>
                    </FormControl>
                    <FormMessage></FormMessage>
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="lastName"
                render={({ field }) => (
                  <FormItem className="sm:col-span-6">
                    <FormLabel>Last name</FormLabel>
                    <FormControl>
                      <Input id="lastName" {...field} disabled={isPending} placeholder="required"></Input>
                    </FormControl>
                    <FormMessage></FormMessage>
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="dob"
                render={({ field }) => (
                  <FormItem className="sm:col-span-6">
                    <FormLabel>Date of birth</FormLabel>
                    <Popover>
                      <PopoverTrigger>
                        <FormControl>
                          <Input
                            className={cn("pl-3 text-left font-normal")}
                            placeholder={"Pick a date"}
                            value={field.value || ""}
                            onChange={field.onChange}
                          ></Input>
                          {/* <Button variant={"outline"} className={cn("w-64 pl-3 text-left font-normal", !field.value && "text-muted-foreground")}>
                             {field.value ? format(field.value, "PPP") : <span>Pick a date</span>}
                             <CalendarIcon className="ml-auto h-4 w-4 opacity-50" />
                            {" "}
                          </Button> */}
                        </FormControl>
                      </PopoverTrigger>
                      <PopoverContent className="w-auto p-0" align="start">
                        <Calendar
                          mode="single"
                          selected={new Date(field.value) || Date.now()}
                          onSelect={(date) => {
                            field.onChange(format(date as Date, "MM/dd/yyyy"));
                          }}
                          disabled={(date) => date > new Date() || date < new Date("1900-01-01")}
                        />
                      </PopoverContent>
                    </Popover>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="telephone"
                render={({ field }) => (
                  <FormItem className="sm:col-span-6">
                    <FormLabel>Telephone</FormLabel>
                    <FormControl>
                      <Input id="telephone" {...field} disabled={isPending} placeholder="optional"></Input>
                    </FormControl>
                    <FormMessage></FormMessage>
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="gender"
                render={({ field }) => (
                  <FormItem className="sm:col-span-12 inline-flex space-x-5 space-y-0 my-2">
                    <FormLabel>Gender</FormLabel>
                    <FormControl>
                      <RadioGroup onValueChange={field.onChange} defaultValue={field.value} className="inline-flex space-x-2 space-y-0">
                        <FormItem className="inline-flex space-x-3 space-y-0">
                          <FormControl>
                            <RadioGroupItem value="MALE" />
                          </FormControl>
                          <FormLabel className="font-normal">Male</FormLabel>
                        </FormItem>
                        <FormItem className="inline-flex space-x-3 space-y-0">
                          <FormControl>
                            <RadioGroupItem value="FEMALE" />
                          </FormControl>
                          <FormLabel className="font-normal">Female</FormLabel>
                        </FormItem>
                      </RadioGroup>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="hispanic"
                render={({ field }) => (
                  <FormItem id="hispanic" className="sm:col-span-6">
                    <FormLabel>Hispanic</FormLabel>
                    <Select
                      {...field}
                      disabled={isPending}
                      onValueChange={(e) => {
                        field.onChange(e);
                        hispanicHandler(e);
                      }}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {HISPANIC.map((h) => (
                          <SelectItem key={h} value={h}>
                            {h}
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
                name="hispanicOther"
                render={({ field }) => (
                  <FormItem className="sm:col-span-6">
                    <FormLabel>Other hispanic</FormLabel>
                    <FormControl>
                      <Input
                        id="hispanicOther"
                        {...field}
                        disabled={isPending || hispanicOtherDisabled}
                        placeholder={form.getValues("hispanic") == "OTHER" ? "required" : "skip"}
                      ></Input>
                    </FormControl>
                    <FormMessage></FormMessage>
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="race"
                render={({ field }) => (
                  <FormItem id="race" className="sm:col-span-6">
                    <FormLabel>Race</FormLabel>
                    <Select
                      {...field}
                      disabled={isPending}
                      onValueChange={(e) => {
                        field.onChange(e);
                        raceHandler(e);
                      }}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {RACE.map((r) => (
                          <SelectItem key={r} value={r}>
                            {r}
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
                name="raceOther"
                disabled={form.getValues().race != "OTHER"}
                render={({ field }) => (
                  <FormItem className="sm:col-span-6">
                    <FormLabel>Other race</FormLabel>
                    <FormControl>
                      <Input
                        id="raceOther"
                        {...field}
                        disabled={isPending || raceOtherDisabled}
                        placeholder={form.getValues("race") == "OTHER" ? "required" : "skip"}
                      ></Input>
                    </FormControl>
                    <FormMessage></FormMessage>
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="otherStay"
                render={({ field }) => (
                  <FormItem id="otherStay" className="sm:col-span-12">
                    <FormLabel>Other stay</FormLabel>
                    <Select {...field} disabled={isPending} onValueChange={field.onChange}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {OTHER_STAY.map((os) => (
                          <SelectItem key={os} value={os}>
                            {os}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
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
                      setEditRecordDialogOpen(false);
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
