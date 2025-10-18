import { CheckCircledIcon } from "@radix-ui/react-icons";

interface FormSuccessProps {
  message?: string;
}

export const FormSuccess = ({ message }: FormSuccessProps) => {
  if (!message) return null;

  return (
    <div
      className="success-message bg-emerald-500/15 p-3 rounded-md flex items-center gap-x-2 text-sm text-emerald-500"
      data-testid="success-message"
    >
      <CheckCircledIcon className="h-4 w-4" />
      <p>{message}</p>
    </div>
  );
};
