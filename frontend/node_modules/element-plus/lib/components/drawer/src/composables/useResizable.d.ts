import type { Ref } from 'vue';
import type { DrawerProps } from '../drawer';
export declare function useResizable(props: DrawerProps, target: Ref<HTMLElement | undefined>): {
    size: import("vue").ComputedRef<string | undefined>;
    isResizing: Ref<boolean>;
    isHorizontal: import("vue").ComputedRef<boolean>;
};
