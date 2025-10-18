import { useState, useTransition } from "react";
import { Description, Dialog, DialogPanel, DialogTitle } from "@headlessui/react";
import { Dispatch, SetStateAction } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { UpdatePasswordSchema } from "@/schemas";
import { z } from "zod";
import { updatePassword } from "@/actions/actionsUser";
import { signOut } from "next-auth/react";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { FormSuccess } from "@/components/form-success";
import { FormError } from "@/components/form-error";
import { Button } from "@/components/ui/button";
import { AuthUser } from "@/types/types";

interface UpdatePasswordDialogProps {
  isEditPasswordOpen: boolean;
  setEditPasswordOpen: Dispatch<SetStateAction<boolean>>;
  authUser: AuthUser;
}

export function UpdatePasswordDialog({ authUser, isEditPasswordOpen, setEditPasswordOpen }: UpdatePasswordDialogProps) {
  const [error, setError] = useState<string | undefined>("");
  const [success, setSuccess] = useState<string | undefined>("");
  const [isPending, startTransition] = useTransition();

  const form = useForm<z.infer<typeof UpdatePasswordSchema>>({
    resolver: zodResolver(UpdatePasswordSchema),
    defaultValues: {
      id: parseInt(authUser.id as string),
      oldPassword: "",
      newPassword: "",
    },
  });

  const onSubmit = (values: z.infer<typeof UpdatePasswordSchema>) => {
    setError("");
    setSuccess("");
    startTransition(() => {
      console.log("values:");
      console.log(values);
      updatePassword(values).then((data) => {
        if (data.error) setError(data.error);
        else {
          setSuccess(data.success);
          signOut();
        }
      });
    });
  };
  return (
    <Dialog open={isEditPasswordOpen} onClose={() => setEditPasswordOpen(false)} className="relative z-50">
      <div className="fixed inset-0 flex w-screen items-center justify-center p-4">
        <DialogPanel className="max-w-lg space-y-4 border bg-white p-12">
          <DialogTitle className="font-bold">Update password</DialogTitle>
          <Description className="text-center">If password update successful then you will be prompted to log in page.</Description>
          <Form {...form}>
            <form name="register-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <div className="space-y-4">
                <FormField
                  control={form.control}
                  name="oldPassword"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Old password</FormLabel>
                      <FormControl>
                        <Input id="old-password" {...field} disabled={isPending} placeholder="******" type="password"></Input>
                      </FormControl>
                      <FormMessage></FormMessage>
                    </FormItem>
                  )}
                ></FormField>
                <FormField
                  control={form.control}
                  name="newPassword"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>New password</FormLabel>
                      <FormControl>
                        <Input id="new-password" {...field} disabled={isPending} placeholder="******" type="password"></Input>
                      </FormControl>
                      <FormMessage></FormMessage>
                    </FormItem>
                  )}
                ></FormField>
              </div>
              <FormError message={error} />
              <FormSuccess message={success} />
              <div className="w-full flex justify-between">
                <Button
                  variant="outline"
                  name="cancel-password-update-button btn"
                  type="reset"
                  className="w-full flex mr-5"
                  disabled={isPending}
                  onClick={(e) => {
                    e.preventDefault();
                    setEditPasswordOpen(false);
                  }}
                >
                  Cancel
                </Button>
                <Button variant="outline" name="password-update-button btn" type="submit" className="w-full flex ml-5" disabled={isPending}>
                  Update
                </Button>
              </div>
            </form>
          </Form>
        </DialogPanel>
      </div>
    </Dialog>
  );
}
