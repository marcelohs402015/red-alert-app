import { motion, AnimatePresence } from 'framer-motion';
import { AlertTriangle, CheckCircle, X, Info } from 'lucide-react';
import Portal from './Portal';

export type ModalType = 'confirm' | 'success' | 'error' | 'info';

interface ConfirmModalProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm?: () => void;
    title: string;
    message: string;
    type?: ModalType;
    confirmText?: string;
    cancelText?: string;
}

/**
 * Custom modal for confirmations and notifications.
 * Uses Portal to render outside DOM hierarchy, ensuring it's always on top.
 */
const ConfirmModal: React.FC<ConfirmModalProps> = ({
    isOpen,
    onClose,
    onConfirm,
    title,
    message,
    type = 'confirm',
    confirmText = 'Confirmar',
    cancelText = 'Cancelar',
}) => {
    const icons = {
        confirm: <AlertTriangle className="w-12 h-12 text-yellow-500" />,
        success: <CheckCircle className="w-12 h-12 text-green-500" />,
        error: <X className="w-12 h-12 text-red-500" />,
        info: <Info className="w-12 h-12 text-blue-500" />,
    };

    const buttonColors = {
        confirm: 'bg-red-600 hover:bg-red-700',
        success: 'bg-green-600 hover:bg-green-700',
        error: 'bg-red-600 hover:bg-red-700',
        info: 'bg-blue-600 hover:bg-blue-700',
    };

    return (
        <Portal>
            <AnimatePresence>
                {isOpen && (
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-[9999]"
                        onClick={onClose}
                    >
                        <motion.div
                            initial={{ scale: 0.9, opacity: 0, y: 20 }}
                            animate={{ scale: 1, opacity: 1, y: 0 }}
                            exit={{ scale: 0.9, opacity: 0, y: 20 }}
                            onClick={(e) => e.stopPropagation()}
                            className="bg-slate-800 rounded-2xl border border-slate-700 p-6 max-w-md w-full mx-4 shadow-2xl"
                        >
                            {/* Icon */}
                            <div className="flex justify-center mb-4">
                                {icons[type]}
                            </div>

                            {/* Title */}
                            <h3 className="text-xl font-bold text-white text-center mb-2">
                                {title}
                            </h3>

                            {/* Message */}
                            <p className="text-gray-400 text-center mb-6">
                                {message}
                            </p>

                            {/* Buttons */}
                            <div className="flex gap-3 justify-center">
                                {type === 'confirm' && (
                                    <button
                                        onClick={onClose}
                                        className="px-6 py-2 bg-slate-600 text-white font-semibold rounded-lg hover:bg-slate-700 transition-colors"
                                    >
                                        {cancelText}
                                    </button>
                                )}
                                <button
                                    onClick={() => {
                                        if (onConfirm) onConfirm();
                                        onClose();
                                    }}
                                    className={`px-6 py-2 text-white font-semibold rounded-lg transition-colors ${buttonColors[type]}`}
                                >
                                    {type === 'confirm' ? confirmText : 'OK'}
                                </button>
                            </div>
                        </motion.div>
                    </motion.div>
                )}
            </AnimatePresence>
        </Portal>
    );
};

export default ConfirmModal;
