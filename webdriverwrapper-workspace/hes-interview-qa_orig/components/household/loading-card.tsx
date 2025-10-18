import { cn } from "@/lib/utils";
import { Skeleton } from "../ui/skeleton";

export default function LoadingCard({ className }: { className?: string }) {
  return (
    <div className={cn(className, " sm:mx-auto sm:w-full sm:col-span-6 lg:float-start border-b-2 rounded-md shadow hover:bg-slate-100 p-5 m-2")}>
      <div className="flex items-center space-x-4 w-full">
        <Skeleton className="h-16 w-16 rounded-full" />
        <div className="space-y-2">
          <Skeleton className="h-4 w-60" />
          <Skeleton className="h-4 w-60" />
          <Skeleton className="h-4 w-40" />
          <Skeleton className="h-4 w-40" />
        </div>
      </div>
    </div>
  );
}
