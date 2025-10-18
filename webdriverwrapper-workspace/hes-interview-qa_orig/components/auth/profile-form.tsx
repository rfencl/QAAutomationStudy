"use client";

import * as z from "zod";
import { useState, useTransition } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { UpdateUserSchema } from "@/schemas";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { FormError } from "@/components/form-error";
import { FormSuccess } from "@/components/form-success";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { CardWrapper } from "@/components/auth/card-wrapper";
import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";

import { AvatarDialog } from "@/components/auth/avatar-dialog";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { updateUser } from "@/actions/actionsUser";
import { UpdatePasswordDialog } from "@/components/auth/update-password-dialog";
import { AuthUser } from "@/types/types";

// export const dynamic = "force-dynamic";

export const ProfileForm = ({ authUser }: { authUser: AuthUser }) => {
  const { update } = useSession();
  const router = useRouter();
  // const [profile, setProfile] = useState(user);
  const [error, setError] = useState<string | undefined>("");
  const [success, setSuccess] = useState<string | undefined>("");
  const [isPending, startTransition] = useTransition();
  const [avatar, setAvatar] = useState<string>(authUser.image as string);
  const [isDialogOpen, setDialogOpen] = useState(false);
  const [isEditOpen, setEditOpen] = useState(false);

  const [isEditPasswordOpen, setEditPasswordOpen] = useState(false);

  const form = useForm<z.infer<typeof UpdateUserSchema>>({
    resolver: zodResolver(UpdateUserSchema),
    defaultValues: {
      id: parseInt(authUser.id as string),
      email: authUser.email as string,
      name: authUser.name as string,
      image: avatar,
    },
  });

  const onSubmit = (values: z.infer<typeof UpdateUserSchema>) => {
    values.image = avatar;
    setError("");
    setSuccess("");
    startTransition(() => {
      console.log("values:");
      console.log(values);
      updateUser(values).then((data) => {
        setError(data.error);
        setSuccess(data.success);
        setEditOpen(false);
      });
      update({
        name: values.name,
        email: values.email,
        image: values.image,
      }).then(() => {
        router.refresh();
      });
    });
  };

  return (
    <CardWrapper>
      <div className="w-full flex justify-between content-center">
        <Avatar>
          <AvatarImage src={avatar} alt="@shadcn" />
          <AvatarFallback>CN</AvatarFallback>
        </Avatar>
        <Button
          variant="link"
          type="button"
          className={!isEditOpen ? "hidden" : ""}
          onClick={() => {
            setDialogOpen(true);
          }}
          disabled={isPending}
        >
          Edit avatar
        </Button>
      </div>
      <AvatarDialog isDialogOpen={isDialogOpen} setDialogOpen={setDialogOpen} setAvatar={setAvatar} />
      <Form {...form}>
        <form name="register-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
          <div className="space-y-4">
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Name</FormLabel>
                  <FormControl>
                    <Input id="name" {...field} disabled={isPending || !isEditOpen} />
                  </FormControl>
                  <FormMessage></FormMessage>
                </FormItem>
              )}
            ></FormField>
            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Email</FormLabel>
                  <FormControl>
                    <Input id="email" {...field} disabled={isPending || !isEditOpen} type="email"></Input>
                  </FormControl>
                  <FormMessage></FormMessage>
                </FormItem>
              )}
            ></FormField>
          </div>
          <FormError message={error} />
          <FormSuccess message={success} />
          {isEditOpen ? (
            <div className="w-full flex justify-between">
              <Button
                variant="outline"
                name="cancel-update-button btn"
                type="reset"
                className="w-full flex mr-5"
                disabled={isPending}
                onClick={(e) => {
                  e.preventDefault();
                  form.reset();
                  setAvatar(authUser.image as string);
                  setEditOpen(false);
                }}
              >
                Cancel
              </Button>
              <Button variant="outline" name="update-button btn" type="submit" className="w-full flex ml-5" disabled={isPending}>
                Update
              </Button>
            </div>
          ) : (
            <Button
              variant="secondary"
              name="edit-button"
              type="button"
              className="w-full"
              disabled={isPending}
              onClick={(e) => {
                e.preventDefault();
                setEditOpen(true);
              }}
            >
              Edit
            </Button>
          )}
        </form>
      </Form>
      <div className="password-container pt-5 flex justify-end">
        <Button variant="link" className="password-edit-button btn flex" type="button" onClick={() => setEditPasswordOpen(true)}>
          Edit password
        </Button>
        <UpdatePasswordDialog authUser={authUser} isEditPasswordOpen={isEditPasswordOpen} setEditPasswordOpen={setEditPasswordOpen} />
      </div>
    </CardWrapper>
  );
};
