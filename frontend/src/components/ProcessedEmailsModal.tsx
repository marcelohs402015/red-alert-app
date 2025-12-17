import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Mail, Trash2, X, RefreshCw, Inbox, Clock, User, FileText, Bell } from 'lucide-react';
import ConfirmModal from './ConfirmModal';
import Portal from './Portal';

/**
 * Processed email from backend.
 */
interface ProcessedEmail {
    id: number;
    emailId: string;
    fromAddress: string;
    subject: string;
    snippet: string;
    receivedAt: string;
    categoryName: string | null;
    categoryId: number | null;
    processedAt: string;
}

/**
 * Props for ProcessedEmailsModal.
 */
interface ProcessedEmailsModalProps {
    isOpen: boolean;
    onClose: () => void;
}

const API_BASE_URL = 'http://localhost:8086/api/v1';

/**
 * Modal component to display and manage processed emails.
 */
const ProcessedEmailsModal: React.FC<ProcessedEmailsModalProps> = ({ isOpen, onClose }) => {
    const [emails, setEmails] = useState<ProcessedEmail[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [confirmModal, setConfirmModal] = useState<{
        isOpen: boolean;
        title: string;
        message: string;
        onConfirm: () => void;
        type: 'danger' | 'warning' | 'info';
    } | null>(null);
    const [simulatingId, setSimulatingId] = useState<number | null>(null);

    const handleSimulateAlert = async (email: ProcessedEmail): Promise<void> => {
        setSimulatingId(email.id);
        try {
            const response = await fetch(`${API_BASE_URL}/alerts/simulate/${email.id}`, {
                method: 'POST',
            });
            if (!response.ok) throw new Error('Failed to simulate alert');

            // Close modal to show the alert overlay
            onClose();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to simulate alert');
        } finally {
            setSimulatingId(null);
        }
    };

    const loadEmails = async (): Promise<void> => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await fetch(`${API_BASE_URL}/processed-emails`);
            if (!response.ok) throw new Error('Failed to fetch processed emails');
            const data: ProcessedEmail[] = await response.json();
            setEmails(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Unknown error');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (isOpen) {
            loadEmails();
        }
    }, [isOpen]);

    const handleDelete = async (id: number): Promise<void> => {
        try {
            const response = await fetch(`${API_BASE_URL}/processed-emails/${id}`, {
                method: 'DELETE',
            });
            if (!response.ok) throw new Error('Failed to delete email');
            setEmails((prev) => prev.filter((e) => e.id !== id));
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to delete');
        }
    };

    const confirmDelete = (email: ProcessedEmail): void => {
        setConfirmModal({
            isOpen: true,
            title: 'Deletar Email Processado',
            message: `Tem certeza que deseja deletar o email "${email.subject}"?`,
            type: 'danger',
            onConfirm: () => {
                handleDelete(email.id);
                setConfirmModal(null);
            },
        });
    };

    const handleDeleteAll = async (): Promise<void> => {
        try {
            const response = await fetch(`${API_BASE_URL}/processed-emails`, {
                method: 'DELETE',
            });
            if (!response.ok) throw new Error('Failed to delete all emails');
            setEmails([]);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to delete all');
        }
    };

    const confirmDeleteAll = (): void => {
        setConfirmModal({
            isOpen: true,
            title: 'Deletar Todos os Emails',
            message: `Tem certeza que deseja deletar TODOS os ${emails.length} emails processados? Esta ação não pode ser desfeita.`,
            type: 'danger',
            onConfirm: () => {
                handleDeleteAll();
                setConfirmModal(null);
            },
        });
    };

    const formatDate = (dateString: string): string => {
        try {
            return new Date(dateString).toLocaleString('pt-BR', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
            });
        } catch {
            return dateString;
        }
    };

    if (!isOpen) return null;

    return (
        <Portal>
            <AnimatePresence>
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4"
                    onClick={onClose}
                >
                    <motion.div
                        initial={{ scale: 0.9, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        exit={{ scale: 0.9, opacity: 0 }}
                        transition={{ type: 'spring', damping: 25, stiffness: 300 }}
                        className="bg-slate-800 rounded-2xl border border-slate-700 shadow-2xl w-full max-w-4xl max-h-[85vh] overflow-hidden"
                        onClick={(e) => e.stopPropagation()}
                    >
                        {/* Header */}
                        <div className="flex items-center justify-between p-6 border-b border-slate-700 bg-slate-800/50">
                            <div className="flex items-center gap-3">
                                <div className="p-2 bg-blue-500/20 rounded-lg">
                                    <Inbox className="w-6 h-6 text-blue-400" />
                                </div>
                                <div>
                                    <h2 className="text-xl font-bold text-white">Emails Processados</h2>
                                    <p className="text-sm text-gray-400">
                                        {emails.length} email(s) encontrado(s)
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2">
                                <button
                                    onClick={loadEmails}
                                    disabled={isLoading}
                                    className="p-2 hover:bg-slate-700 rounded-lg transition-colors disabled:opacity-50"
                                    title="Atualizar"
                                    type="button"
                                >
                                    <RefreshCw className={`w-5 h-5 text-gray-400 ${isLoading ? 'animate-spin' : ''}`} />
                                </button>
                                {emails.length > 0 && (
                                    <button
                                        onClick={confirmDeleteAll}
                                        className="px-3 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-400 rounded-lg transition-colors flex items-center gap-2 text-sm"
                                        type="button"
                                    >
                                        <Trash2 className="w-4 h-4" />
                                        Limpar Todos
                                    </button>
                                )}
                                <button
                                    onClick={onClose}
                                    className="p-2 hover:bg-slate-700 rounded-lg transition-colors"
                                    type="button"
                                >
                                    <X className="w-5 h-5 text-gray-400" />
                                </button>
                            </div>
                        </div>

                        {/* Content */}
                        <div className="p-6 overflow-y-auto max-h-[calc(85vh-120px)]">
                            {isLoading && (
                                <div className="flex items-center justify-center py-12">
                                    <RefreshCw className="w-8 h-8 text-blue-400 animate-spin" />
                                </div>
                            )}

                            {error && (
                                <div className="bg-red-500/20 border border-red-500/30 rounded-lg p-4 text-red-400">
                                    {error}
                                </div>
                            )}

                            {!isLoading && !error && emails.length === 0 && (
                                <div className="text-center py-12">
                                    <Mail className="w-16 h-16 text-gray-600 mx-auto mb-4" />
                                    <p className="text-gray-400 text-lg">Nenhum email processado ainda</p>
                                    <p className="text-gray-500 text-sm mt-2">
                                        Os emails serão salvos aqui quando forem detectados pelo sistema
                                    </p>
                                </div>
                            )}

                            {!isLoading && !error && emails.length > 0 && (
                                <div className="space-y-3">
                                    {emails.map((email) => (
                                        <motion.div
                                            key={email.id}
                                            initial={{ opacity: 0, y: 10 }}
                                            animate={{ opacity: 1, y: 0 }}
                                            exit={{ opacity: 0, y: -10 }}
                                            className="bg-slate-700/50 hover:bg-slate-700 rounded-xl p-4 border border-slate-600/50 transition-colors group"
                                        >
                                            <div className="flex items-start justify-between gap-4">
                                                <div className="flex-1 min-w-0">
                                                    {/* Subject */}
                                                    <h3 className="font-semibold text-white truncate mb-2">
                                                        {email.subject || '(Sem assunto)'}
                                                    </h3>

                                                    {/* From */}
                                                    <div className="flex items-center gap-2 text-sm text-gray-400 mb-2">
                                                        <User className="w-4 h-4 flex-shrink-0" />
                                                        <span className="truncate">{email.fromAddress}</span>
                                                    </div>

                                                    {/* Snippet */}
                                                    {email.snippet && (
                                                        <div className="flex items-start gap-2 text-sm text-gray-500 mb-3">
                                                            <FileText className="w-4 h-4 flex-shrink-0 mt-0.5" />
                                                            <p className="line-clamp-2">{email.snippet}</p>
                                                        </div>
                                                    )}

                                                    {/* Meta */}
                                                    <div className="flex flex-wrap items-center gap-4 text-xs text-gray-500">
                                                        {email.categoryName && (
                                                            <span className="px-2 py-1 bg-blue-500/20 text-blue-400 rounded-full">
                                                                {email.categoryName}
                                                            </span>
                                                        )}
                                                        <div className="flex items-center gap-1">
                                                            <Clock className="w-3 h-3" />
                                                            <span>Recebido: {formatDate(email.receivedAt)}</span>
                                                        </div>
                                                        <div className="flex items-center gap-1">
                                                            <Clock className="w-3 h-3" />
                                                            <span>Processado: {formatDate(email.processedAt)}</span>
                                                        </div>
                                                    </div>
                                                </div>

                                                {/* Action buttons */}
                                                <div className="flex flex-col gap-2">
                                                    <button
                                                        onClick={() => handleSimulateAlert(email)}
                                                        disabled={simulatingId === email.id}
                                                        className="p-2 opacity-0 group-hover:opacity-100 hover:bg-orange-500/20 rounded-lg transition-all disabled:opacity-50"
                                                        title="Simular Alerta"
                                                        type="button"
                                                    >
                                                        <Bell className={`w-5 h-5 text-orange-400 ${simulatingId === email.id ? 'animate-pulse' : ''}`} />
                                                    </button>
                                                    <button
                                                        onClick={() => confirmDelete(email)}
                                                        className="p-2 opacity-0 group-hover:opacity-100 hover:bg-red-500/20 rounded-lg transition-all"
                                                        title="Deletar"
                                                        type="button"
                                                    >
                                                        <Trash2 className="w-5 h-5 text-red-400" />
                                                    </button>
                                                </div>
                                            </div>
                                        </motion.div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </motion.div>
                </motion.div>
            </AnimatePresence>

            {/* Confirm Modal */}
            {confirmModal && (
                <ConfirmModal
                    isOpen={confirmModal.isOpen}
                    title={confirmModal.title}
                    message={confirmModal.message}
                    type={confirmModal.type}
                    onConfirm={confirmModal.onConfirm}
                    onClose={() => setConfirmModal(null)}
                />
            )}
        </Portal>
    );
};

export default ProcessedEmailsModal;
