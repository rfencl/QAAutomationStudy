import { Description, Dialog, DialogPanel, DialogTitle } from "@headlessui/react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { avatarLinks } from "@/lib/avatars";
import { Dispatch, SetStateAction } from "react";

interface AvatarDialogProps {
  isDialogOpen: boolean;
  setDialogOpen: Dispatch<SetStateAction<boolean>>;
  setAvatar: Dispatch<SetStateAction<string>>;
}

export function AvatarDialog({ setAvatar, isDialogOpen, setDialogOpen }: AvatarDialogProps) {
  return (
    <Dialog open={isDialogOpen} onClose={() => setDialogOpen(false)} className="relative z-50">
      <div className="fixed inset-0 flex w-screen items-center justify-center p-4">
        <DialogPanel className="max-w-lg space-y-4 border bg-white p-12">
          <DialogTitle className="font-bold">Pick avatar</DialogTitle>
          <Description>This avatar will be displayed in your profile</Description>
          <div className="avatar-selection flex flex-wrap">
            {avatarLinks.map((a, i) => (
              <div className="flex p-2" key={i}>
                <Avatar
                  onClick={() => {
                    console.log(a);
                    setAvatar(a);
                    setDialogOpen(false);
                  }}
                >
                  <AvatarImage src={a} alt="@shadcn" />
                  <AvatarFallback>CN</AvatarFallback>
                </Avatar>
              </div>
            ))}
          </div>
        </DialogPanel>
      </div>
    </Dialog>
  );
}
